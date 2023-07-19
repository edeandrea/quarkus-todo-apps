package com.acme.todo.rest;

import java.util.Map;

import org.testcontainers.containers.BindMode;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class WiremockResourceTestLifecycleManager implements QuarkusTestResourceLifecycleManager {
	private final WireMockContainer wireMockContainer =
		new WireMockContainer(DockerImageName.parse("wiremock/wiremock:2.35.0-1"))
			.withClasspathResourceMapping("wiremock", "/home/wiremock", BindMode.READ_ONLY);
//			.withMapping("twitter",  "wiremock/mappings/twitter.com-current-stubs.json");

	@Override
	public Map<String, String> start() {
		this.wireMockContainer.start();
		return Map.of("quarkus.rest-client.twitter-api.url", this.wireMockContainer.getUrl("/"));
	}

	@Override
	public void stop() {
		this.wireMockContainer.stop();
	}
}
