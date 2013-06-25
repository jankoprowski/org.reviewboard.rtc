package org.reviewboard.rtc;

import com.ibm.team.repository.service.AbstractService;


public class ReviewBoardConfigurationProperties extends AbstractService implements IReviewBoardConfigurationProperties {

	@Override
	public String getUsername() {
		return getStringConfigProperty("username");
	}

	@Override
	public String getPassword() {
		return getStringConfigProperty("password");
	}

}
