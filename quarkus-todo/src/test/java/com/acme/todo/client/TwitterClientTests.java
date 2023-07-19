package com.acme.todo.client;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import com.acme.todo.client.TwitterClient.Tweet;
import com.acme.todo.client.TwitterClient.TweetResponse;
import com.acme.todo.rest.WiremockResourceTestLifecycleManager;

@QuarkusTest
@QuarkusTestResource(WiremockResourceTestLifecycleManager.class)
class TwitterClientTests {
	@Inject
	@RestClient
	TwitterClient twitterClient;

	@Test
	public void sendTweet() {
		var response = this.twitterClient.sendTweet(new Tweet("Go on vacation!"));

		assertThat(response)
			.isNotNull()
			.extracting(TweetResponse::data)
			.isNotNull();

		assertThat(response.data())
			.isNotNull()
			.extracting(Tweet::text)
			.isEqualTo("Go on vacation!");
	}
}