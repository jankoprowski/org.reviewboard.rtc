package org.reviewboard.rtc.rest;

public class ReportWarningException extends Exception {

	public final String summary;
	public final String description;
	private static final long serialVersionUID = 6856699086231785514L;

	ReportWarningException(String summary, String description) {
		super(summary);
		this.summary = summary;
		this.description = description;
	}

	public ReportWarningException(String message) {
		this(message, message);
	}

}
