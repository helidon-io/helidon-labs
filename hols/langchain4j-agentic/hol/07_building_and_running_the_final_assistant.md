# 7. Building and Running the Final Assistant

In this section, we will:

- Build and run your updated `code/bootstrap` project.
- Validate agent routing and tool usage behavior.
- Verify your project matches the final implementation.

---

## 1. Build and run

From `hols/langchain4j-agentic/code/bootstrap`:

```sh
mvn clean package
java -jar target/helidon-agentic-assistant.jar
```

## 2. Validate ingestion status

In another terminal:

```sh
curl -X GET http://localhost:8080/progress
```

Wait until `remaining` reaches `0`.

## 3. Validate chat

Ask an SE question:

```sh
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"How do I start a Helidon SE quickstart project?","summary":""}'
```

Ask an MP question:

```sh
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"How does dependency injection work in Helidon MP?","summary":""}'
```

Ask for latest version (tool call):

```sh
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is the latest Helidon version?","summary":""}'
```

You should see `4.4.0-FROM-TOOL`.

## 4. Verify your bootstrap project is now equal to `code/final`

From `hols/langchain4j-agentic/code`:

```sh
diff -ru --exclude target basic final
```

Expected result: no differences.

---

### Next Step -> [Bonus: Using MCP Server Instead of Local Tools](08_bonus_using_mcp_server.md)

