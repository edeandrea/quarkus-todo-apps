package com.acme.todo.rest;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Duration;

import jakarta.ws.rs.core.Response.Status;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;

import com.acme.todo.domain.TodoEntity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

@QuarkusIntegrationTest
@QuarkusTestResource(KafkaCompanionResource.class)
@TestMethodOrder(OrderAnnotation.class)
public class TodoResourceIT {
	private static final String COMPLETIONS_TOPIC_NAME = "todocompletions";
	private static final TodoEntity TODO = new TodoEntity(0L, "My first todo", true);
	private static final int DEFAULT_ORDER = 0;

	@InjectKafkaCompanion
	KafkaCompanion kafkaCompanion;

	@BeforeEach
	public void beforeEach() {
		this.kafkaCompanion.registerSerde(
			TodoEntity.class,
			new ObjectMapperSerializer<>(),
			new ObjectMapperDeserializer<>(TodoEntity.class)
		);
	}

	@Test
	@Order(DEFAULT_ORDER)
	void findAll() {
		var todos = get("/todo").then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.extract().body()
			.jsonPath().getList(".", TodoEntity.class);

		assertThat(todos)
			.singleElement()
			.usingRecursiveComparison()
			.isEqualTo(TODO);
	}

	@Test
	@Order(DEFAULT_ORDER)
	void findByIdFound() {
		var foundTodo = get("/todo/{id}", TODO.getId()).then()
			.statusCode(Status.OK.getStatusCode())
			.contentType(ContentType.JSON)
			.extract().as(TodoEntity.class);

		assertThat(foundTodo)
			.isNotNull()
			.usingRecursiveComparison()
			.isEqualTo(TODO);
	}

	@Test
	@Order(DEFAULT_ORDER)
	void findByIdNotFound() {
		get("/todo/{id}", -1).then()
			.statusCode(Status.NO_CONTENT.getStatusCode());
	}

	@Test
	@Order(DEFAULT_ORDER + 1)
	void delete() {
		RestAssured.delete("/todo/{id}", TODO.getId()).then()
			.statusCode(Status.NO_CONTENT.getStatusCode())
			.body(blankOrNullString());

		checkTodoSize(0);
	}

	@Test
	@Order(DEFAULT_ORDER + 2)
	void createNonCompleted() {
		var completions = this.kafkaCompanion.consume(TodoEntity.class)
      .withOffsetReset(OffsetResetStrategy.EARLIEST)
      .withGroupId(COMPLETIONS_TOPIC_NAME)
      .withAutoCommit()
      .fromTopics(COMPLETIONS_TOPIC_NAME, 0);

		var todo = new TodoEntity();
		todo.setTitle(TODO.getTitle());

		var createdTodo = given()
			.body(todo)
			.contentType(ContentType.JSON)
			.post("/todo").then()
			.statusCode(Status.OK.getStatusCode())
			.contentType(ContentType.JSON)
			.extract().as(TodoEntity.class);

		assertThat(createdTodo)
			.isNotNull()
			.usingRecursiveComparison()
			.ignoringFields("id")
			.isEqualTo(todo);

		assertThat(createdTodo.getId())
			.isNotNull()
			.isPositive();

		checkTodoSize(1);

		completions.awaitNoRecords(Duration.ofSeconds(10));
	}

	@Test
	@Order(DEFAULT_ORDER + 3)
	void createCompleted() {
		var completions = this.kafkaCompanion.consume(TodoEntity.class)
      .withOffsetReset(OffsetResetStrategy.EARLIEST)
      .withGroupId(COMPLETIONS_TOPIC_NAME)
      .withAutoCommit()
      .fromTopics(COMPLETIONS_TOPIC_NAME, 1);

		var todo = new TodoEntity();
		todo.setTitle(TODO.getTitle());
		todo.setCompleted(true);

		var createdTodo = given()
			.body(todo)
			.contentType(ContentType.JSON)
			.post("/todo").then()
			.statusCode(Status.OK.getStatusCode())
			.contentType(ContentType.JSON)
			.extract().as(TodoEntity.class);

		assertThat(createdTodo)
			.isNotNull()
			.usingRecursiveComparison()
			.ignoringFields("id")
			.isEqualTo(todo);

		assertThat(createdTodo.getId())
			.isNotNull()
			.isPositive();

		checkTodoSize(2);

		var todoCompletion = completions.awaitCompletion(Duration.ofSeconds(10))
			.getFirstRecord()
			.value();

		assertThat(todoCompletion)
			.isNotNull()
			.usingRecursiveComparison()
			.isEqualTo(createdTodo);
	}

	@Test
	@Order(DEFAULT_ORDER + 4)
	void updatedToNonCompleted() {
		var completions = this.kafkaCompanion.consume(TodoEntity.class)
      .withOffsetReset(OffsetResetStrategy.EARLIEST)
      .withGroupId(COMPLETIONS_TOPIC_NAME)
      .withAutoCommit()
      .fromTopics(COMPLETIONS_TOPIC_NAME, 0);

		var nonCompletedTodo = get("/todo").then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.extract().body()
			.jsonPath().getList(".", TodoEntity.class)
			.stream()
			.filter(TodoEntity::isCompleted)
			.findAny();

		assertThat(nonCompletedTodo)
			.isPresent();

		var todo = new TodoEntity(nonCompletedTodo.get().getId(), nonCompletedTodo.get().getTitle(), false);

		given()
			.body(todo)
			.contentType(ContentType.JSON)
			.put("/todo").then()
			.statusCode(Status.NO_CONTENT.getStatusCode());

		checkTodoSize(2);

		completions.awaitNoRecords(Duration.ofSeconds(10));
	}

	@Test
	@Order(DEFAULT_ORDER + 5)
	void updatedToCompleted() {
		var completions = this.kafkaCompanion.consume(TodoEntity.class)
      .withOffsetReset(OffsetResetStrategy.EARLIEST)
      .withGroupId(COMPLETIONS_TOPIC_NAME)
      .withAutoCommit()
      .fromTopics(COMPLETIONS_TOPIC_NAME, 1);

		var completedTodo = get("/todo").then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.extract().body()
			.jsonPath().getList(".", TodoEntity.class)
			.stream()
			.filter(todo -> !todo.isCompleted())
			.findAny();

		assertThat(completedTodo)
			.isPresent();

		var todo = new TodoEntity(completedTodo.get().getId(), completedTodo.get().getTitle(), true);

		given()
			.body(todo)
			.contentType(ContentType.JSON)
			.put("/todo").then()
			.statusCode(Status.NO_CONTENT.getStatusCode());

		checkTodoSize(2);

		var todoCompletion = completions.awaitCompletion(Duration.ofSeconds(10))
			.getFirstRecord()
			.value();

		assertThat(todoCompletion)
			.isNotNull()
			.usingRecursiveComparison()
			.isEqualTo(todo);
	}

	private static void checkTodoSize(int expectedSize) {
		get("/todo").then()
			.statusCode(Status.OK.getStatusCode())
			.contentType(ContentType.JSON)
			.body("size()", is(expectedSize));
	}
}
