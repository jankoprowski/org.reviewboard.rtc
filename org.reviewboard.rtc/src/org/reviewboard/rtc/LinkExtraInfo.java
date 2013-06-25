package org.reviewboard.rtc;

import java.sql.Timestamp;

public class LinkExtraInfo {

	public String updateUrl;
	public Timestamp lastCodeChange;

	public LinkExtraInfo(String updateUrl) {
		this.updateUrl = updateUrl;

	}
}
