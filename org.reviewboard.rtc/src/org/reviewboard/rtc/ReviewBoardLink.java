package org.reviewboard.rtc;

import com.ibm.team.links.common.ILink;

public class ReviewBoardLink {

	String url;
	ILink link;
	LinkExtraInfo extraInfo;

	public ReviewBoardLink(ILink link) {
		this("", "", link);
	}

	public ReviewBoardLink(String url, String updateUrl, ILink link) {
		this(url, link, new LinkExtraInfo(updateUrl));
	}

	public ReviewBoardLink(String url, String updateUrl) {
		this(url, updateUrl, null);
	}

	public ReviewBoardLink(String url, ILink link, LinkExtraInfo extraInfo) {
		this.url = url;
		this.link = link;
		this.extraInfo = extraInfo;
	}

	public ReviewBoardLink(ILink link, LinkExtraInfo extraInfo) {
		this("", link, extraInfo);
	}
}