package com.acme.todo.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "twitter-api")
public interface TwitterClient {
	record Tweet(String id, String text) {
		public Tweet(String text) {
			this("", text);
		}
	}

	record TweetResponse(Tweet data) { }

	@POST
	@Path("/2/tweets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	TweetResponse sendTweet(Tweet tweet);
}
