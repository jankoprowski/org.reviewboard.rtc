package org.reviewboard.rtc;

import com.ibm.team.process.common.advice.IProcessReport;
import com.ibm.team.process.common.advice.IReportInfo;
import com.ibm.team.process.common.advice.runtime.IParticipantInfoCollector;

public class TeamAdvisor {

	private final IParticipantInfoCollector collector;

	public TeamAdvisor(IParticipantInfoCollector collector) {
		this.collector = collector;
	}

	public void error(String message) {
		error(message, message);
	}

	public void error(String summary, String description) {
		message(summary, description, IProcessReport.ERROR);
	}

	public void warning(String summary, String description) {
		message(summary, description, IProcessReport.WARNING);
	}

	public void info(String message) {
		message(message, IProcessReport.OK);
	}

	private void message(String message, int severity) {
		message(message, message, severity);
	}

	private void message(String summary, String description, int severity) {
		IReportInfo info = collector.createInfo(summary, description);
		info.setSeverity(severity);
		collector.addInfo(info);
	}

}
