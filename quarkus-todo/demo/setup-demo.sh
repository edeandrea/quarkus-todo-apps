#!/bin/bash -e

# Make all the directories
echo "Creating all the directories"
mkdir -p src/main/java/com/acme/todo/client
mkdir -p src/main/java/com/acme/todo/domain
mkdir -p src/main/java/com/acme/todo/repository
mkdir -p src/main/java/com/acme/todo/rest
mkdir -p src/main/resources/META-INF/resources
rm -rf src/main/resources/application.properties
mkdir -p src/test/java/com/acme/todo/client
mkdir -p src/test/java/com/acme/todo/rest
mkdir -p src/test/resources/com/acme/todo/WiremockResourceTestLifecycleManager

# Fetch starter files
echo "Fetching starter files"
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/initialCode/pom.xml -o pom.xml
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/java/com/acme/todo/domain/TodoEntity.java -o src/main/java/com/acme/todo/domain/TodoEntity.java
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/java/com/acme/todo/repository/TodoRepository.java -o src/main/java/com/acme/todo/repository/TodoRepository.java
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/initialCode/TodoResource.java -o src/main/java/com/acme/todo/rest/TodoResource.java
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/initialCode/application.yml -o src/main/resources/application.yml
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/resources/import.sql -o src/main/resources/import.sql
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/resources/META-INF/resources/index.html -o src/main/resources/META-INF/resources/index.html
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/resources/META-INF/resources/todo-component.html -o src/main/resources/META-INF/resources/todo-component.html
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/resources/META-INF/resources/todo-component.js -o src/main/resources/META-INF/resources/todo-component.js
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/resources/META-INF/resources/todo.css -o src/main/resources/META-INF/resources/todo.css
curl -Ls https://raw.githubusercontent.com/edeandrea/quarkus-todo-apps/main/quarkus-todo/src/main/resources/META-INF/resources/todo.js -o src/main/resources/META-INF/resources/todo.js