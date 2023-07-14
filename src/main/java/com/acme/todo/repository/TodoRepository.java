package com.acme.todo.repository;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import com.acme.todo.domain.TodoEntity;

@ApplicationScoped
public class TodoRepository implements PanacheRepository<TodoEntity> {}
