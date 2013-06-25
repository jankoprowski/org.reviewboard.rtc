package org.reviewboard.rtc;

import com.ibm.team.repository.common.TeamRepositoryException;

public interface IDiffGenerator {

	public abstract StringBuilder generateDiff(WorkItemInformationRetriever workItem) throws TeamRepositoryException;

}