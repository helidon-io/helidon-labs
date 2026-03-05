# 2. Setting Up the Bootstrap Project

In this section, we will:

- Build and run the bootstrap project from `code/bootstrap`.
- Verify REST endpoints and UI.
- Establish the starting point before we transform it into the final agentic app.

---

## 1. Open the bootstrap project

```sh
cd hols/langchain4j-agentic/code/bootstrap
```

## 2. Build the project

```sh
mvn clean package
```

## 3. Run the bootstrap application

```sh
java -jar target/helidon-basic-assistant.jar
```

## 4. Verify endpoints

In a second terminal:

```sh
curl -X GET http://localhost:8080/progress
```

And test chat:

```sh
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is Helidon SE?","summary":""}'
```

## 5. Verify web UI

Open:

```text
http://localhost:8080
```

Stop the app after verification.

---

### Next Step -> [Configuring the Project for Agentic AI](03_configuring_the_project_for_agentic_ai.md)

