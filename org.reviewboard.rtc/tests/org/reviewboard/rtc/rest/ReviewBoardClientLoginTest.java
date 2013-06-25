package org.reviewboard.rtc.rest;
import org.junit.Test;
import org.reviewboard.rtc.ReviewBoardClient;
import org.reviewboard.rtc.ReviewBoardRepository;

import com.sun.jersey.api.client.ClientResponse;


public class ReviewBoardClientLoginTest extends ReviewBoardTestCase {

	@Test
	public void testLogin_correctCredentials_200OK() {

		ReviewBoardRepository repo = getBrokenCredentialsRepo(username, password);
		ReviewBoardClient reviewBoard = new ReviewBoardClient(repo);
		ClientResponse response = reviewBoard.login();

		int expected = 200;
		int actual = response.getStatus();

		assertEquals(expected, actual);
	}

	@Test
	public void testLogin_wrongUsername_Error401Unauthorized() {

		String username = "wrongUsername";

		ReviewBoardRepository repo = getBrokenCredentialsRepo(username, password);
		ReviewBoardClient reviewBoard = new ReviewBoardClient(repo);
		ClientResponse response = reviewBoard.login();

		int expected = 401;
		int actual = response.getStatus();

		assertEquals(expected, actual);
	}

	@Test
	public void testLogin_wrongPassword_Error401Unauthorized() {

		String password = "wrongPassword";

		ReviewBoardRepository repo = getBrokenCredentialsRepo(username, password);
		ReviewBoardClient reviewBoard = new ReviewBoardClient(repo);
		ClientResponse response = reviewBoard.login();

		int expected = 401;
		int actual = response.getStatus();

		assertEquals(expected, actual);
	}

	@Test
	public void testLogin_wrongResource_Error404NotFound() {

		String address = "http://zlomek:8080/api!/";

		ReviewBoardRepository repo = getBrokenAddressRepo(address);
		ReviewBoardClient reviewBoard = new ReviewBoardClient(repo);
		ClientResponse response = reviewBoard.login();

		int expected = 404;
		int actual = response.getStatus();

		assertEquals(expected, actual);
	}

}
