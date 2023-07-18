package org.acme;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.quarkus.logging.Log;

@ApplicationScoped
public class TodoCompletionListener {
	static final String INCOMING_TOPIC_NAME = "todocompletions";

	@Incoming(INCOMING_TOPIC_NAME)
	public void todoCompleted(Todo todo) {
		Log.infof("Todo completed: %s", todo);
	}
}
