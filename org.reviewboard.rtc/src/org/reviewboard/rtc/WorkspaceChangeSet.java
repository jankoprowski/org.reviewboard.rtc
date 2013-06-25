package org.reviewboard.rtc;

import com.ibm.team.scm.common.IChangeSet;

public class WorkspaceChangeSet {
	public WorkspaceChangeSet(IChangeSet changeSet, String workspace) {
		this.changeSet = changeSet;
		this.workspace = workspace;
	}
	public IChangeSet changeSet;
	public String workspace;
}
