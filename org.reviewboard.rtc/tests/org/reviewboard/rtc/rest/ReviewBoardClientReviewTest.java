package org.reviewboard.rtc.rest;

import org.junit.Test;
import org.reviewboard.rtc.ReviewBoardClient;
import org.reviewboard.rtc.ReviewBoardRepository;


public class ReviewBoardClientReviewTest extends ReviewBoardTestCase {

	private ReviewBoardClient reviewboard;

	@Override
	public void setUp() {
		ReviewBoardRepository repo = getWorkingRepo();
		reviewboard = new ReviewBoardClient(repo);
		reviewboard.login();
	}

//	@Test
//	public void testCreateReview_reviewCreatedSuccessfully_successExpected() throws Exception {
//
//		ReviewParameters review = new ReviewParameters();
//
//		review._public = true;
//		review.summary = "testing summary";
//		review.branch = "testing branch";
//		review.description = "testing description";
//		review.bugs_closed = new String[] { "1234" };
//		review.target_people = new String[] { username };
//
//		Response response = reviewboard.generateReview(review);
//
//		OutputReviewRequest actual = response.review_request;
//		ReviewParameters expected = review;
//
//		assertEquals(expected.summary, actual.summary);
//		assertEquals(expected.description, actual.description);
//		assertEquals(expected.branch, actual.branch);
//		assertEquals(expected.target_people[0], actual.target_people[0].title);
//
//		Assert.assertArrayEquals(expected.bugs_closed, actual.bugs_closed);
//	}
//
//	@Test
//	public void testCreateReview_testSubmitAsAnotherUser_successExpected() throws Exception {
//
//		ReviewParameters review = new ReviewParameters();
//
//		review._public = true;
//		review.summary = "testing summary";
//		review.branch = "testing branch";
//		review.description = "testing description";
//		review.bugs_closed = new String[] { "1234" };
//		review.target_people = new String[] { username };
//		review.submit_as = "rtc";
//
//		Response response = reviewboard.generateReview(review);
//
//		String actual = response.review_request.links.submitter.title;
//		String expected = review.submit_as;
//
//		assertEquals(expected, actual);
//	}

	@Test
	public void testGetCcmAddress_repositoryNameExists_successExpected() throws Exception {

		String expected = "https://zlomek:9443/ccm";
		String actual = reviewboard.getCcmAddress();

		assertEquals(expected, actual);
	}
}
