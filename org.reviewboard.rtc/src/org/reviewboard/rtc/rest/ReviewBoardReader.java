package org.reviewboard.rtc.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import com.google.gson.Gson;

public class ReviewBoardReader implements MessageBodyReader<Response> {

	private final Gson gson = new Gson();

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public Response readFrom(Class<Response> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
	throws IOException, WebApplicationException {

		if (isJson(type, genericType, annotations, mediaType)) {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream));
			return gson.fromJson(reader, Response.class);
		}

		return new Response();
	}

	private boolean isJson(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return "application".equals(mediaType.getType()) && mediaType.getSubtype().startsWith("vnd.reviewboard.org");
	}
}
