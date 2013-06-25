package org.reviewboard.rtc.rest;

import org.reviewboard.rtc.rb.output.OutputRepositoriesRequest;
import org.reviewboard.rtc.rb.output.OutputReviewRequest;


public class Response {
	public String stat;
	public OutputReviewRequest review_request;
	public OutputRepositoriesRequest[] repositories;
	public OutputReviewRequest draft;
}
