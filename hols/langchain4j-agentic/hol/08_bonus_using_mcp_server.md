# 8. Bonus: Using MCP Server Instead of Local Tools

In this section, we will:

- Build and run the MCP server in a separate terminal.
- Switch the assistant from local `@Ai.Tools` to `@Ai.McpClients`.
- Run and verify end-to-end MCP tool integration.

---

## 1. Build and run the MCP server (Terminal 1)

```sh
cd hols/langchain4j-agentic/code/mcp-server
```

```sh
mvn clean package
```

```sh
java -jar target/helidon-mcp-weather-server-declarative.jar
```

Server runs on:

```text
http://localhost:8081/cli
```

## 2. Enable MCP client in assistant config (Terminal 2)

In `hols/langchain4j-agentic/code/bootstrap/src/main/resources/application.yaml`,
uncomment this block:

```yaml
  mcp-clients:
    cli-tools-mcp-server:
      uri: http://localhost:8081/cli
```
> [!WARNING]
> Check the indentation in the YAML file! Added key should look like this `langchain4j.mcp-clients.cli-tools-mcp-server.uri`


## 3. Switch expert agents to MCP clients

### Update `HelidonSeExpert.java`

Replace:

```java
import io.helidon.hol.agentic.assistant.tools.CliTools;
...
@Ai.Tools(CliTools.class)
```

with:

```java
@Ai.McpClients("cli-tools-mcp-server")
```

### Update `HelidonMpExpert.java`

Replace:

```java
@Ai.Tools(CliTools.class)
```

with:

```java
@Ai.McpClients("cli-tools-mcp-server")
```

## 4. Build and run the assistant

```sh
cd hols/langchain4j-agentic/code/bootstrap
```

```sh
mvn clean package
```

```sh
java -jar target/*.jar
```

## 5. Verify MCP tools are used

Open the chat UI at http://localhost:8080

You should see Helidon Assistant prompt UI with a progress bar showing RAG ingestion state.

![hol-ui-progressbar.png](img/hol-ui-progressbar.png)

When the ingestion is finished, the progressbar will disappear.

Example prompt:
```
Show me how to create a new Helidon SE application named javaone-demo with cli showing a HTTP resource with Hello World with latest Helidon version.
```

Expected CLI provided by the assistant should look like this:

```text
helidon init --batch \
  --version 4.4.0-FROM-MCP-SERVER \
  --name javaone-demo \
  -Dpackage=com.example.javaonedemo \
  -Dflavor=SE \
  -Dapp-type=quickstart
```

This confirms the app is using MCP-based tools instead of local tool methods.

You can check the MCP server system output to see whether MCP server was called:
```
INFO Latest released Helidon version: 4.4.0-FROM-MCP-SERVER
INFO Init with Helidon cli cmd called, version: 4.4.0-FROM-MCP-SERVER, project name: javaone-demo
```

---

### Congratulations! You Have Completed the Agentic HOL.

