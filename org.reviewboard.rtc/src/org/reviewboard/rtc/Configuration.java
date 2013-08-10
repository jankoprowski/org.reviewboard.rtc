package org.reviewboard.rtc;

import org.reviewboard.rtc.rest.ReportWarningException;

import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.workitem.common.ISaveParameter;
import com.ibm.team.workitem.common.internal.workflow.WorkflowInfo;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;

/**
 * Wrap Advanced Properties and Participant Configuration in one class.
 *
 * @author jkoprows
 *
 */
public class Configuration {

	private static final String MISSING_CONFIGURATION_MESSAGE = "'%s' configuration element is missing. Configure follow-up using eclipse plugin.";
	private static final String REPOSITORY = "repository";
	private static final String STATE = "state";
	private static final String TYPE = "type";
	private static final String CHECKS = "checks";
	private static final String ADDRESS = "address";
	private static final String RESOLUTION = "resolution";
	private static final String NAME = "name";

	private final IProcessConfigurationElement config;
	private final IReviewBoardConfigurationProperties properties;

	public Configuration(IProcessConfigurationElement config, IReviewBoardConfigurationProperties properties) {
		this.config = config;
		this.properties = properties;
	}

	public ReviewBoardRepository createRepositoryObject() throws Exception {
		String username = properties.getUsername();
		String password = properties.getPassword();
		String address = getAddress();
		String name = getName();
		return new ReviewBoardRepository(username, password, address, name);

	}

	/**
	 * Check is save parameters fulfill Participant Configuration.
	 *
	 * @param param - save parameters
	 * @return true when parameters match to configuration, false otherwise
	 */
	public boolean check(ISaveParameter param) {

		IWorkItem oldState = (IWorkItem) param.getOldState();
		IWorkItem newState = (IWorkItem) param.getNewState();

		if (oldState == null) {
			return false;
		}

		Identifier<IState> oldStateId = oldState.getState2();
		Identifier<IState> newStateId = newState.getState2();

		if (oldStateId.equals(newStateId))
			return false;

		for (IProcessConfigurationElement check : getChecks()) {
			if (evaluate(check, newState)) {
				return true;
			}
		}

		return false;
	}

	private boolean evaluate(IProcessConfigurationElement check, IWorkItem workItem) {

		String typeId= check.getAttribute(TYPE);
		String stateId= check.getAttribute(STATE);
		String resolutionId= check.getAttribute(RESOLUTION);

		boolean matches= true;

		matches&= workItem.getWorkItemType().equals(typeId);

		if (stateId != null) {
			String realStateId= WorkflowInfo.stripOffPrefix(stateId, 's');
			String workItemState= workItem.getState2() != null ? workItem.getState2().getStringIdentifier() : null;
			matches&= realStateId.equals(workItemState);
		}

		if (resolutionId != null) {
			String realResolutionId= WorkflowInfo.stripOffPrefix(resolutionId, 'r');
			String workItemResolution= workItem.getResolution2() != null ? workItem.getResolution2().getStringIdentifier() : null;
			matches&= realResolutionId.equals(workItemResolution);
		}

		return matches;
	}

	private IProcessConfigurationElement[] getChecks() {
		IProcessConfigurationElement[] checks = getChild(config, CHECKS).getChildren();
		if (checks == null) {
			String.format(MISSING_CONFIGURATION_MESSAGE, CHECKS);
		}
		return checks;
	}

	private String getAddress() throws ReportWarningException {
		return getRepository().getAttribute(ADDRESS);
	}

	private String getName() throws ReportWarningException {
		return getRepository().getAttribute(NAME);
	}

	private IProcessConfigurationElement getRepository() throws ReportWarningException {
		IProcessConfigurationElement repository = getChild(config, REPOSITORY);
		if (repository == null) {
			throw new ReportWarningException(String.format(MISSING_CONFIGURATION_MESSAGE, REPOSITORY));
		}
		return repository;
	}

	private IProcessConfigurationElement getChild(IProcessConfigurationElement element, String name) {
		for (IProcessConfigurationElement cfg : element.getChildren()) {
			if (cfg.getName().equals(name))
				return cfg;
		}
		return null;
	}

}
