package org.reviewboard.rtc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.reviewboard.rtc.rb.input.ReviewParameters;
import org.reviewboard.rtc.rest.ReportWarningException;
import org.reviewboard.rtc.rest.Response;

import com.ibm.team.links.common.service.ILinkService;
import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.process.common.advice.AdvisableOperation;
import com.ibm.team.process.common.advice.runtime.IOperationParticipant;
import com.ibm.team.process.common.advice.runtime.IParticipantInfoCollector;
import com.ibm.team.repository.common.IAuditable;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.AbstractService;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.scm.common.IScmService;
import com.ibm.team.scm.common.internal.IScmQueryService;
import com.ibm.team.scm.service.IServerSideVersionedContentService;
import com.ibm.team.workitem.common.ISaveParameter;
import com.ibm.team.workitem.common.model.IWorkItem;

public class ReviewWorkItemParticipant extends AbstractService implements IOperationParticipant {

	private static final String NO_CHANGE_SETS_TO_REVIEW = "No change sets to review.";
	private static final String REVIEW_GENERATED_MESSAGE = "Review was generated under %s.";

	@Override
	public void run(AdvisableOperation operation, IProcessConfigurationElement participantConfig, IParticipantInfoCollector collector, IProgressMonitor monitor)
			throws TeamRepositoryException {

		Object data = operation.getOperationData();
		if (!(data instanceof ISaveParameter)) {
			return;
		}

		ISaveParameter param = (ISaveParameter) data;
		IAuditable auditable = param.getNewState();
		if (!(auditable instanceof IWorkItem)) {
			return;
		}

		IWorkItem workItem = (IWorkItem) auditable;

		IReviewBoardConfigurationProperties reviewBoardConfig = getService(IReviewBoardConfigurationProperties.class); // Handle exception
		Configuration config = new Configuration(participantConfig, reviewBoardConfig);

		if (config.check(param)) {
			TeamAdvisor teamAdvisor = new TeamAdvisor(collector);
			generateReview(config, teamAdvisor, monitor, workItem);
		}
	}

	private void generateReview(final Configuration config, final TeamAdvisor teamAdvisor, final IProgressMonitor monitor, final IWorkItem iWorkItem) {

		String url = "";

		try {

			WorkItemInformationRetriever workItem = constructReviewWorkItem(iWorkItem, teamAdvisor);
			if (workItem.getChangeSets().isEmpty()) {
				teamAdvisor.info(NO_CHANGE_SETS_TO_REVIEW);
				return;
			}

			Response resp;
			StringBuilder diff;
			ReviewBoardRepository repo = config.createRepositoryObject();
			ReviewBoardLink link = workItem.getLink(repo.address);
			ReviewBoardClient client = new ReviewBoardClient(repo);
			client.login();

			UnifiedDiffGenerator diffGenerator = constructDiffGenerator(monitor);

			ReviewParameters reviewParameters = client.reviewParametersFromWorkItem(workItem);
			if (link == null) {
				resp = client.createReview(reviewParameters);
				diff = diffGenerator.generateDiff(workItem);
				client.uploadDiff(resp, diff);
			} else {
				resp = client.getReview(link);
				if (workItem.codeChangedSinceLastReview(link)) {
					diff = diffGenerator.generateDiff(workItem);
					client.uploadDiff(resp, diff);
				}
			}

			link = client.updateReview(reviewParameters, resp, link);
			workItem.updateReviewLink(link);

			url = link.url;


		} catch (ReportWarningException e) {
			teamAdvisor.warning(e.summary, e.description);
			return;
		} catch (Exception e) {
			teamAdvisor.error(e.getMessage());
			return;
		}

		if (!url.isEmpty()) {
			teamAdvisor.info(String.format(REVIEW_GENERATED_MESSAGE, url));
		}
	}

	private WorkItemInformationRetriever constructReviewWorkItem(IWorkItem rtcWorkItem, TeamAdvisor teamAdvisor) {
		ILinkService linkService = getService(ILinkService.class);
		IRepositoryItemService repositoryItemService = getService(IRepositoryItemService.class);
		WorkItemInformationRetriever workItem = new WorkItemInformationRetriever(rtcWorkItem, repositoryItemService, linkService, teamAdvisor);
		return workItem;
	}

	private UnifiedDiffGenerator constructDiffGenerator(IProgressMonitor monitor) {
		IScmService scmService = getService(IScmService.class);
		IScmQueryService scmQueryService = getService(IScmQueryService.class);
		IServerSideVersionedContentService versionedContentService = getService(IServerSideVersionedContentService.class);
		return new UnifiedDiffGenerator(scmService, scmQueryService, versionedContentService, monitor);
	}

}
