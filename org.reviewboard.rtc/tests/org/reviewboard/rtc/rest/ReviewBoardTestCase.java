package org.reviewboard.rtc.rest;

import org.reviewboard.rtc.ReviewBoardRepository;

import junit.framework.TestCase;

public class ReviewBoardTestCase extends TestCase {

	protected static final String username = "reviewboard";
	protected static final String password = "password";
	protected static final String address = "http://reviewboard:8080/api/";
	protected static final String name = "RTC";

	protected ReviewBoardRepository getBrokenCredentialsRepo(String username, String password) {
		return new ReviewBoardRepository(username, password, address, name);
	}

	protected ReviewBoardRepository getBrokenAddressRepo(String address) {
		return new ReviewBoardRepository(username, password, address, name);
	}

	protected ReviewBoardRepository getWorkingRepo() {
		return new ReviewBoardRepository(username, password, address, name);
	}
}
