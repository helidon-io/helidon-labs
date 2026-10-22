# 2. Setting Up the Bootstrap Project

In this section, we will:

- Build and run the bootstrap project from `code/bootstrap`.
- Verify REST endpoints and UI.
- Establish the starting point before we transform it into the final agentic app.

---

## 1. Open the bootstrap project

```sh
cd langchain4j-agentic/code/bootstrap
```

## 2. Build the project

The supplied POM uses the Helidon `27.0.0` application parent and Java release `27`.
LangChain4j integration dependencies use `io.helidon.extensions.langchain4j` group IDs and
`helidon-extensions-langchain4j-*` artifact IDs, managed by
`io.helidon.extensions.langchain4j:helidon-extensions-langchain4j-bom:27.0.0`.
The LangChain4j code generator uses the same extension version. Direct LangChain4j dependencies are managed by
`dev.langchain4j:langchain4j-bom:1.18.1`.

```sh
mvnw clean package
```

## 3. Run the bootstrap application

```sh
java -jar target/helidon-basic-assistant.jar
```

Startup downloads `embeddings.zip` if any embedding file is missing and extracts `all-embeddings.json`, `se-embeddings.json`, and `mp-embeddings.json` into the working directory, preserving existing files. Bootstrap loads `all-embeddings.json`.

## 4. Verify web UI

Open the chat UI at http://localhost:8080

You should see Helidon Assistant prompt UI.

![hol-ui.png](img/hol-ui.png)


Example prompt:
```
Show me how to create a new Helidon SE application named javaone-demo with cli showing a HTTP resource with Hello World.
```

> [!NOTE]
> Notice that Helidon assistant has no specialization, AI service uses a single big embedding store
for all the prompts regardless of the Helidon flavor (SE or MP) the user is asking for.
Response mixes all the context it gets from the emebeding store together.

Stop the app after verification (return to your terminal and type CTRL-C).


---

### Next Step → [Configuring the Project for Agentic AI](03_configuring_the_project_for_agentic_ai.md)

