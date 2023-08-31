package org.acme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.stream.LongStream;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.InMemoryLogHandler;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;

import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
public class TodoCompletionListenerTests {
	private static final String COMPLETED_TODO_TEXT = "Completed todo #";
	private static final InMemoryLogHandler LOG_HANDLER = new InMemoryLogHandler(logRecord -> true);

	static {
		LogManager.getLogManager().getLogger("org.acme").addHandler(LOG_HANDLER);
	}

	@InjectKafkaCompanion
	KafkaCompanion kafkaCompanion;

	@BeforeEach
	public void beforeEach() {
		this.kafkaCompanion.registerSerde(
			TodoCompletionListener.Todo.class,
			new ObjectMapperSerializer<>(),
			new ObjectMapperDeserializer<>(TodoCompletionListener.Todo.class)
		);
	}

	@Test
	void listenerWorks() {
		// Create 10 records & send them to the channel
		this.kafkaCompanion.produce(TodoCompletionListener.Todo.class)
			.fromRecords(
				LongStream.range(0, 10)
					.mapToObj(id -> new ProducerRecord<String, TodoCompletionListener.Todo>(TodoCompletionListener.INCOMING_TOPIC_NAME, new TodoCompletionListener.Todo(id, COMPLETED_TODO_TEXT + id)))
					.toList()
			);

		// Wait for our logs to appear
		// There could be a time delay between the messages getting to kafka & the consumer
		await()
			.atMost(Duration.ofMinutes(5))
			.pollDelay(Duration.ofSeconds(1))
			.pollInterval(Duration.ofSeconds(1))
			.until(() -> getLogs().size() == 10);

		// Grab the logs for the TodoCompletionListener class
		var completionListenerLogs = getLogs();

		// Verify there are 10 logs
		assertThat(completionListenerLogs)
			.isNotNull()
			.hasSize(10)
			.extracting(r -> r.getParameters()[0])
			.doesNotContainNull();

		// Get the actual Todo objects that were passed as parameters
		var todos = completionListenerLogs.stream()
			.map(LogRecord::getParameters)
			.filter(Objects::nonNull)
			.map(params -> params[0])
			.filter(Objects::nonNull)
			.map(TodoCompletionListener.Todo.class::cast)
			.toList();

		// Assert the todos match what we expect
		assertThat(todos)
			.isNotNull()
			.hasSize(10)
			.extracting(TodoCompletionListener.Todo::title)
			.allMatch(s -> s.startsWith(COMPLETED_TODO_TEXT));
	}

	private static List<LogRecord> getLogs() {
		return LOG_HANDLER.getRecords()
			.stream()
			.filter(logRecord -> logRecord.getLoggerName().equals(TodoCompletionListener.class.getName()))
			.toList();
	}
}
