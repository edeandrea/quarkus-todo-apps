package com.acme.todo.rest;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;

import com.acme.todo.domain.TodoEntity;
import com.acme.todo.repository.TodoRepository;

@Path("/todo")
@Produces(MediaType.APPLICATION_JSON)
public class TodoResource {
	private final TodoRepository todoRepository;

	public TodoResource(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	@GET
	public List<TodoEntity> findAll() {
		return this.todoRepository.listAll(Sort.by("id", Direction.Descending));
	}

	@GET
	@Path("/{id}")
	public TodoEntity findById(@PathParam("id") Long id) {
		return this.todoRepository.findById(id);
	}

	@PUT
	@Transactional
	public void update(TodoEntity resource) {
		this.todoRepository.findByIdOptional(resource.getId())
			.map(todo -> {
				todo.setCompleted(resource.getCompleted());
				todo.setTitle(resource.getTitle());

				return todo;
			});
	}

	@POST
	@Transactional
	public TodoEntity create(TodoEntity resource) {
		this.todoRepository.persist(resource);
		return resource;
	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public void delete(@PathParam("id") Long id) {
		this.todoRepository.deleteById(id);
	}
}
