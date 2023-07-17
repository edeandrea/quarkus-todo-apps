package com.acme.todo.rest;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.ParameterizedTest.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import com.acme.todo.domain.TodoEntity;
import com.acme.todo.repository.TodoRepository;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;

@QuarkusTest
class TodoResourceTests {
	private static final TodoEntity TODO = new TodoEntity(1L, "Go on vacation", false);
	private static final String COMPLETIONS_CHANNEL_NAME = "todocompletions";

	@InjectMock
	TodoRepository todoRepository;

	@Inject
	@Any
	InMemoryConnector inMemoryConnector;

	@BeforeEach
	public void beforeEach() {
		this.inMemoryConnector.sink(COMPLETIONS_CHANNEL_NAME).clear();
	}

	@Test
	void findAll() {
		when(this.todoRepository.listAll(any(Sort.class)))
			.thenReturn(List.of(TODO));

		var todos = get("/todo").then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.extract().body()
			.jsonPath().getList(".", TodoEntity.class);

		assertThat(todos)
			.singleElement()
			.usingRecursiveComparison()
			.isEqualTo(TODO);

		verify(this.todoRepository).listAll(any(Sort.class));
		verifyNoMoreInteractions(this.todoRepository);
	}

	@Test
	void findById() {
		when(this.todoRepository.findById(eq(TODO.getId())))
			.thenReturn(TODO);

		var foundTodo = get("/todo/{id}", TODO.getId()).then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.extract().as(TodoEntity.class);

		assertThat(foundTodo)
			.isNotNull()
			.usingRecursiveComparison()
			.isEqualTo(TODO);

		verify(this.todoRepository).findById(eq(TODO.getId()));
		verifyNoMoreInteractions(this.todoRepository);
	}

	@ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + INDEX_PLACEHOLDER + "] (" + ARGUMENTS_WITH_NAMES_PLACEHOLDER + ")")
	@ValueSource(booleans = { true, false })
	void update(boolean completed) {
		var todo = new TodoEntity(TODO.getId(), TODO.getTitle(), completed);

		when(this.todoRepository.findByIdOptional(eq(todo.getId())))
			.thenReturn(Optional.of(todo));

		given()
			.body(todo)
			.contentType(ContentType.JSON)
			.put("/todo").then()
			.statusCode(204);

		var emittedMessages = this.inMemoryConnector.sink(COMPLETIONS_CHANNEL_NAME).received();

		if (completed) {
			assertThat(emittedMessages)
				.isNotNull()
				.singleElement()
				.extracting(Message::getPayload)
				.usingRecursiveComparison()
				.isEqualTo(todo);
		}
		else {
			assertThat(emittedMessages)
				.isNullOrEmpty();
		}

		verify(this.todoRepository).findByIdOptional(eq(todo.getId()));
		verifyNoMoreInteractions(this.todoRepository);
	}

	@ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + INDEX_PLACEHOLDER + "] (" + ARGUMENTS_WITH_NAMES_PLACEHOLDER + ")")
	@ValueSource(booleans = { true, false })
	void create(boolean completed) {
		doNothing()
			.when(this.todoRepository)
			.persist(any(TodoEntity.class));

		var todo = new TodoEntity(TODO.getId(), TODO.getTitle(), completed);

		var createdTodo = given()
			.body(todo)
			.contentType(ContentType.JSON)
			.post("/todo").then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.extract().as(TodoEntity.class);

		assertThat(createdTodo)
			.isNotNull()
			.usingRecursiveComparison()
			.isEqualTo(todo);

		var emittedMessages = this.inMemoryConnector.sink(COMPLETIONS_CHANNEL_NAME).received();

		if (completed) {
			assertThat(emittedMessages)
				.isNotNull()
				.singleElement()
				.extracting(Message::getPayload)
				.usingRecursiveComparison()
				.isEqualTo(todo);
		}
		else {
			assertThat(emittedMessages)
				.isNullOrEmpty();
		}

		verify(this.todoRepository).persist(any(TodoEntity.class));
		verifyNoMoreInteractions(this.todoRepository);
	}

	@Test
	void delete() {
		when(this.todoRepository.deleteById(anyLong()))
			.thenReturn(true);

		given().delete("/todo/{id}", TODO.getId()).then()
			.statusCode(204);

		verify(this.todoRepository).deleteById(eq(TODO.getId()));
		verifyNoMoreInteractions(this.todoRepository);
	}
}