package org.reviewboard.rtc;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.reviewboard.rtc.rb.input.InputDraft;
import org.reviewboard.rtc.rb.input.ReviewParameters;
import org.reviewboard.rtc.rb.output.OutputRepositoriesRequest;
import org.reviewboard.rtc.rest.ReportWarningException;
import org.reviewboard.rtc.rest.Response;
import org.reviewboard.rtc.rest.ReviewBoardBasicAuthCookieFilter;
import org.reviewboard.rtc.rest.ReviewBoardReader;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

public class ReviewBoardClient {

	private static final String MULTIPART_BODY_NAME = "path\"; filename=\"review.diff";
	private static final String SESSION_PATH = "session/";
	private static final String REVIEW_REQUESTS_PATH = "review-requests/";
	private static final String REPOSITORIES_PATH = "repositories/";
	private static final String TOOL = "RTC";

	private WebResource service;
	private final ReviewBoardRepository repo;
	private final String api;

	public ReviewBoardClient(ReviewBoardRepository repo) {
		this.repo = repo;
		this.api = repo.address + "api/";
	}

	public ClientResponse login() {

		ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(ReviewBoardReader.class);

		Client client = Client.create(config);
		client.setFollowRedirects(true);
		client.addFilter(new ReviewBoardBasicAuthCookieFilter());
		client.addFilter(new HTTPBasicAuthFilter(repo.username, repo.password));

		service = client.resource(api);
		return service.path(SESSION_PATH).get(ClientResponse.class);
	}

	public void uploadDiff(Response response, StringBuilder diff) {
		String diffUrl = normalizePath(response.review_request.links.diffs.href);
		FormDataMultiPart multiPartForm = diffParametersForm(diff);
		service.path(diffUrl).type(MediaType.MULTIPART_FORM_DATA)
										.accept(MediaType.APPLICATION_JSON)
										.post(Response.class, multiPartForm);
	}

	public ReviewBoardLink updateReview(ReviewParameters review, Response response, ReviewBoardLink link) {

		String draftUrl = normalizePath(response.review_request.links.draft.href);

		Form form;
		form = reviewParametersForm(review);
		service.path(draftUrl).accept(MediaType.APPLICATION_JSON).put(Response.class, form);

		String url = repo.address + "r/" + response.review_request.id;
		String updateUrl = response.review_request.links.self.href;

		if (link == null) {
			link = new ReviewBoardLink(url, updateUrl);
		} else {
			link.url = url;
			link.extraInfo.updateUrl = updateUrl;
		}


		return link;
	}

	public Response getReview(ReviewBoardLink link) {
		String url = normalizePath(link.extraInfo.updateUrl);
		return service.path(url).accept(MediaType.APPLICATION_JSON).get(Response.class);
	}

	public Response createReview(ReviewParameters review) throws Exception {
		Form form;
		Response response;
		form = formFromReview(review);
		response = service.path(REVIEW_REQUESTS_PATH).accept(MediaType.APPLICATION_JSON).post(Response.class, form);
		return response;
	}


	public String normalizePath(String path) {
		return path.replace(service.getURI().toString(), "");
	}

	public String normalizeField(String[] array) {

		String field = "";

		for (int i = 0; i < array.length; i++) {
			field += array[i];
			if (i < array.length - 1) {
				field += ", ";
			}
		}

		return field;

	}

	public ReviewParameters reviewParametersFromWorkItem(WorkItemInformationRetriever workItem) throws TeamRepositoryException, URISyntaxException, ReportWarningException {

		ReviewParameters request = new ReviewParameters();

		request.submit_as = workItem.getOwner();
		request.summary = workItem.getSummary();
		request.description =  workItem.getDescription();
		request.bugs_closed = new String[] { workItem.getId() };
		request.target_people = workItem.getReviewers();
		request._public = true;

		return request;
	}

	public String getCcmAddress() throws Exception {

		Response response = service.path(REPOSITORIES_PATH).accept(MediaType.APPLICATION_JSON).get(Response.class);

		for (OutputRepositoriesRequest repo : response.repositories) {
			if (TOOL.equals(repo.tool) && this.repo.name.equals(repo.name)) {
				return repo.path;
			}
		}

		throw new ReportWarningException(String.format("Repository '%s' was not found at '%s'.", repo.name, repo.address));
	}


	private FormDataMultiPart diffParametersForm(StringBuilder diff) {

		FormDataMultiPart form = new FormDataMultiPart();
		ByteArrayInputStream diffInputStream = new ByteArrayInputStream(String.valueOf(diff).getBytes());
		FormDataBodyPart fdp = new FormDataBodyPart(MULTIPART_BODY_NAME, diffInputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		form.bodyPart(fdp);

		return form;
	}

	private Form reviewParametersForm(InputDraft review) {

		String bugs_closed = normalizeField(review.bugs_closed);
		String target_people = normalizeField(review.target_people);

		Form form = new Form();
		form.add("summary", review.summary);
		form.add("branch", review.branch);
		form.add("description", review.description);
		form.add("bugs_closed", bugs_closed);
		form.add("public", review._public);
		form.add("target_people", target_people);

		return form;
	}


	private Form formFromReview(ReviewParameters review) throws Exception {

		Form form = new Form();
		form.add("submit_as", review.submit_as);
		form.add("repository", getCcmAddress());

		return form;

	}
}
