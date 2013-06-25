package org.reviewboard.rtc.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.NewCookie;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ReviewBoardBasicAuthCookieFilter extends ClientFilter {

	private final List<Object> cookies = new ArrayList<Object>();

	@Override
	public ClientResponse handle(ClientRequest request)
			throws ClientHandlerException {

		ClientResponse response;
		List<NewCookie> cookies;

		if (this.cookies.isEmpty()) {
			response = getNext().handle(request);
			cookies = response.getCookies();
			this.cookies.addAll(cookies);
		}

		request.getHeaders().put("Cookie", this.cookies);
		return getNext().handle(request);
	}

}
