This is the TODO app that comes with a simple UI.

Whenever a todo is completed, a message is sent over Kafka which is received by the [`quarkus-todo-listener` app](../quarkus-todo-listener) and processed (for now, just logged to the console).

Additionally, the completion is [tweeted using the Twitter REST API](https://developer.twitter.com/en/docs/twitter-api/tweets/manage-tweets/api-reference/post-tweets).

The [Wiremock Testcontainers integration](https://testcontainers.com/modules/wiremock/) is used for testing purposes.
- The [WireMock Twitter API Template](https://library.wiremock.org/catalog/api/twitter-com-current/) is used to stub the Twitter API.
- See [`WiremockResourceTestLifecycleManager.java`](src/test/java/com/acme/todo/WiremockResourceTestLifecycleManager.java) for the integration as a `QuarkusTestResourceLifecycleManager`.

![Quarkus todos](images/quarkus-todos.png)