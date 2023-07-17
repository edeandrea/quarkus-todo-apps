Quarkus Todo application. Used in demos about testcontainers integration. This repo has multiple sub-projects:

- [`quarkus-todo`](quarkus-todo)
    - A simple Quarkus todo app that publishes completion events to an Apache Kafka topic
- [`quarkus-todo-listener`](quarkus-todo-listener)
    - A Kafka listener app that listens for events published by [`quarkus-todo`](quarkus-todo) 