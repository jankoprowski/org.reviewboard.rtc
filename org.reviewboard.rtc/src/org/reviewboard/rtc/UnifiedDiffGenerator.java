package org.reviewboard.rtc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.ibm.team.filesystem.common.FileLineDelimiter;
import com.ibm.team.filesystem.common.IFileContent;
import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.filesystem.common.IFileItemHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.common.UUID;
import com.ibm.team.scm.common.IChangeSet;
import com.ibm.team.scm.common.IFolderHandle;
import com.ibm.team.scm.common.IRepositoryProgressMonitor;
import com.ibm.team.scm.common.IScmService;
import com.ibm.team.scm.common.IVersionable;
import com.ibm.team.scm.common.IVersionableHandle;
import com.ibm.team.scm.common.IWorkspaceHandle;
import com.ibm.team.scm.common.dto.IAncestorReport;
import com.ibm.team.scm.common.dto.INameItemPair;
import com.ibm.team.scm.common.dto.IWorkspaceSearchCriteria;
import com.ibm.team.scm.common.internal.Change;
import com.ibm.team.scm.common.internal.IScmQueryService;
import com.ibm.team.scm.common.internal.dto.ItemQueryResult;
import com.ibm.team.scm.common.internal.dto.ServiceConfigurationProvider;
import com.ibm.team.scm.service.IServerSideVersionedContentService;

import difflib.DiffUtils;
import difflib.Patch;

public class UnifiedDiffGenerator implements IDiffGenerator {

	private static final int PARENT_ELEMENT = 1;
	private static final int FILE_ELEMENT = 0;
	private static final int HANDLED_WORKSPACES = 1;
	private static final String DEV_NULL = "/dev/null";

	private final IRepositoryProgressMonitor monitor;
	private final IScmService scmService;
	private final IScmQueryService scmQueryService;
	private final IServerSideVersionedContentService versionedContentService;
	private WorkItemInformationRetriever workItem;

	public UnifiedDiffGenerator(IScmService scmService, IScmQueryService scmQueryService, IServerSideVersionedContentService versionedContentService, IProgressMonitor monitor) {
		this.scmService = scmService;
		this.scmQueryService = scmQueryService;
		this.versionedContentService = versionedContentService;
		this.monitor = IRepositoryProgressMonitor.ITEM_FACTORY.createItem(monitor);
	}

	/* (non-Javadoc)
	 * @see org.reviewboard.rtc.IDiffGenerator#generateDiff(org.reviewboard.rtc.ReviewWorkItem)
	 */
	@Override
	public StringBuilder generateDiff(WorkItemInformationRetriever workItem) throws TeamRepositoryException {

		this.workItem = workItem;

		List<String> lines = new ArrayList<String>();

		Map<UUID, FileChange> changes = calculateChanges();
		IWorkspaceSearchCriteria criteria = workItem.getWorkspaceCriteria();

		StringBuilder diff = new StringBuilder();

		String eol = System.getProperty("line.separator"); // Determine one style of eof independentem from operating system

		diff.append("### Review Patch 1.0" + eol);

		for (FileChange fileChange : changes.values()) {

			DiffInfo before = fileChangeToDiffInfo(monitor, criteria, fileChange.before);
			DiffInfo after = fileChangeToDiffInfo(monitor, criteria, fileChange.after);

			if (before.binary || after.binary) {
				lines.add("--- " + before.header);
				lines.add("+++ " + after.header);
				lines.add("Binary files were modified.");
			} else {
				Patch patch = DiffUtils.diff(before.lines, after.lines);
				List<String> unifiedDiff = DiffUtils.generateUnifiedDiff(before.header, after.header, before.lines, patch, 3);
				lines.addAll(unifiedDiff);
			}

			if (fileChange.before.handle != null) {
				String itemId = fileChange.before.handle.getItemId().getUuidValue();
				String stateId = fileChange.before.handle.getStateId().getUuidValue();
				diff.append("#before_state: \"" + before.filename + "\" \"" + itemId + "\" \"" + stateId + "\"" + eol);
			}
		}

		for (String line : lines)
		{
			diff.append(line);
			diff.append(eol);
		}

		return diff;
	}

	private DiffInfo fileChangeToDiffInfo(IRepositoryProgressMonitor monitor, IWorkspaceSearchCriteria criteria, FileState state)
			throws TeamRepositoryException {

		if (state.handle == null)
			return new DiffInfo();

		return generateFileInfo(state, criteria, monitor);
	}



	private DiffInfo generateFileInfo(FileState state, IWorkspaceSearchCriteria criteria, IRepositoryProgressMonitor monitor) throws TeamRepositoryException {

		boolean binary = false;
		String filename = "";
		Date timestamp;
		List<String> lines = new ArrayList<String>();

		IFileItem file = (IFileItem) scmService.fetchState(state.handle, IScmService.COMPLETE, null);

		filename = getFullPathFileName(file, state, criteria, monitor);
		timestamp = file.getFileTimestamp();
		binary = isBinary(file);

		if (binary) {
			new DiffInfo(filename, lines, timestamp, binary);
		} else {
			lines = getContent(file, state.handle);
		}

		return new DiffInfo(filename, lines, timestamp, binary);

	}

	@SuppressWarnings("unchecked")
	private String getFullPathFileName(IFileItem file, FileState state, IWorkspaceSearchCriteria criteria, IRepositoryProgressMonitor monitor)
			throws TeamRepositoryException {

		IAncestorReport reports[] = getAncestorReports(file, state.changeSet, criteria, monitor);
		List<INameItemPair> nameItems = reports[FILE_ELEMENT].getNameItemPairs();

		if (nameItems.isEmpty() && (file.getParent() != null)) { // Deleted files also needs parent's path
			nameItems = reports[PARENT_ELEMENT].getNameItemPairs();
		}

		return combinePath(file, nameItems);
	}

	private IAncestorReport[] getAncestorReports(IFileItem file, IChangeSet changeSet, IWorkspaceSearchCriteria criteria, IRepositoryProgressMonitor monitor)
			throws TeamRepositoryException {

		IVersionableHandle[] handles = getVersionabelHandles(file);
		ItemQueryResult result = scmQueryService.findWorkspacesForChangeset(changeSet, criteria, HANDLED_WORKSPACES, monitor);

		if (result.getItemHandles().isEmpty()) {
			return new IAncestorReport[0];
		}

		IWorkspaceHandle workspace = (IWorkspaceHandle) result.getItemHandles().get(0);
		ServiceConfigurationProvider config = ServiceConfigurationProvider.FACTORY.create(workspace, changeSet.getComponent());
		return scmService.configurationLocateAncestors(config, handles, null, monitor);
	}

	private IVersionableHandle[] getVersionabelHandles(IFileItem file) {

		List<IVersionableHandle> handles = new ArrayList<IVersionableHandle>();
		handles.add(file);

		IFolderHandle parent = file.getParent();
		if (parent != null) {
			handles.add(parent);
		}

		return handles.toArray(new IVersionableHandle[0]);
	}

	private List<String> getContent(IFileItem file, IVersionableHandle handle) throws TeamRepositoryException {

		List<String> lines;

		IFileContent content = file.getContent();
		ByteArrayOutputStream contents = new ByteArrayOutputStream();
		versionedContentService.fetchContent(handle, content.getHash(), contents);
		lines = contentToLines(contents.toString());

		return lines;
	}

	@SuppressWarnings("unchecked")
	private Map<UUID, FileChange> calculateChanges() throws TeamRepositoryException {

		List<IChangeSet> changeSets = workItem.getChangeSets();

		Map<UUID, FileChange> fileChanges = new HashMap<UUID, FileChange>();

		for (IChangeSet changeSet : changeSets) {

			if (changeSet == null) { // Access to change set is denied.
				continue;
			}

			Date lastChangeDate = changeSet.getLastChangeDate();
			List<Change> changes = changeSet.changes();

			for (Change change : changes) {

				if (change.item() instanceof IFileItemHandle) {

					IVersionableHandle beforeState = change.beforeState();
					IVersionableHandle afterState = change.afterState();

					UUID itemId = getStateItemId(beforeState, afterState);
					FileChange fileChange = fileChanges.get(itemId);

					if (fileChange == null) {
						fileChanges.put(itemId, new FileChange(beforeState, afterState, lastChangeDate, changeSet));
					} else if (lastChangeDate.before(fileChange.getBeforeStateDate())) {
						fileChange.updateBeforeState(beforeState, lastChangeDate, changeSet);
					} else if (lastChangeDate.after(fileChange.getAfterStateDate())) {
						fileChange.updateAfterState(afterState, lastChangeDate, changeSet);
					}

				}
			}
		}
		return fileChanges;
	}

	private UUID getStateItemId(IVersionableHandle beforeState, IVersionableHandle afterState) {

		if (afterState == null)
			return beforeState.getItemId();

		return afterState.getItemId();
	}

	private boolean isBinary(IFileItem fileItem) {
		String contentType = fileItem.getContentType();
		FileLineDelimiter delimiter = fileItem.getContent().getLineDelimiter();
		return FileLineDelimiter.LINE_DELIMITER_NONE.equals(delimiter)
				|| contentType.startsWith("audio")
				|| contentType.startsWith("video")
				|| contentType.startsWith("image")
				|| contentType.endsWith("unknown");
	}


	private String combinePath(IVersionable file, List<INameItemPair> chunks) {

		StringBuffer path = new StringBuffer();
		String filename = file.getName();

		if (chunks.isEmpty()) {
			return filename;
		}

		for (INameItemPair chunk : chunks) {
			if (path.length() != 0)
				path.append(Path.SEPARATOR);

			// Ignore the root folder which doesn't have a name
			if (chunk.getName() != null)
				path.append(chunk.getName());
		}

		if (path.indexOf(filename) == -1) {
			path.append(Path.SEPARATOR);
			path.append(filename);
		}

		return path.toString();
	}

	private static List<String> contentToLines(String contents) {

		String line = "";
		List<String> lines = new LinkedList<String>();

		try {
			BufferedReader in = new BufferedReader(new StringReader(contents));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			lines.add("While reading content IOException occured:");
			lines.add(e.getMessage());
		}

		return lines;
	}

	private class DiffInfo {

		private static final String TAB = "\t";
		private static final String SIMPLE_DATE_FORMAT_TIMESTAMP = "yyyy-MM-dd'T'HH:mm'Z'";
		public final String header;
		public final String filename;
		public final List<String> lines;
		public final boolean binary;
		private final DateFormat iso8601;


		DiffInfo(String filename, List<String> lines, Date timestamp, boolean binary) {
			this.filename = filename;
			this.lines = lines;
			this.binary = binary;

			this.iso8601 = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TIMESTAMP);
			this.header = filename + TAB + iso8601.format(timestamp);
		}

		DiffInfo() {
			this(DEV_NULL, Collections.<String> emptyList(), new Date(), false); // TODO: try to get Date() from e.g. changeset
		}
	}

	private class FileState {
		public Date date;
		public IVersionableHandle handle;
		public IChangeSet changeSet;

		FileState(IVersionableHandle handle, IChangeSet changeSet, Date date) {
			this.handle = handle;
			this.changeSet = changeSet;
			this.date = date;
		}
	}

	private class FileChange {

		public FileState before;
		public FileState after;

		public FileChange(IVersionableHandle beforeHandle, IVersionableHandle afterHandle, Date lastChangeDate, IChangeSet changeSet) {
			this.before = new FileState(beforeHandle, changeSet, lastChangeDate);
			this.after = new FileState(afterHandle, changeSet, lastChangeDate);
		}

		public void updateBeforeState(IVersionableHandle beforeHandle, Date beforeStateDate, IChangeSet changeSet) {
			this.before.handle = beforeHandle;
			this.before.date = beforeStateDate;
			this.before.changeSet = changeSet;
		}

		public void updateAfterState(IVersionableHandle afterHandle, Date afterStateDate, IChangeSet changeSet) {
			this.after.handle = afterHandle;
			this.after.date = afterStateDate;
			this.after.changeSet = changeSet;
		}

		public Date getBeforeStateDate() {
			return this.before.date;
		}

		public Date getAfterStateDate() {
			return this.after.date;
		}
	}

}
