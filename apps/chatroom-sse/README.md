# Helidon Labs Apps ChatRoom SSE

This example demonstrates how to use Server Sent Events (SSE) with both the `WebServer`
and `WebClient` APIs.

This project implements a simple chat room web application.
The unit test uses the `WebClient` API to test the endpoints.

## Build, run and test

Build and start the server:
```shell
mvn package
java -jar target/helidon-labs-apps-chatroom-sse.jar
```

Then open <http://localhost:8080> in your browser.
