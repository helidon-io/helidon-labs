# 8. Bonus: Using MCP Server Instead of Local Tools

In this section, we will:

- Build and run the MCP server in a separate terminal.
- Switch the assistant from local `@Ai.Tools` to `@Ai.McpClients`.
- Run and verify end-to-end MCP tool integration.

---

## 1. Build and run the MCP server (Terminal 1)

```sh
cd hols/langchain4j-agentic/code/mcp-server
mvn clean package
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
mvn clean package
java -jar target/helidon-agentic-assistant.jar
```

## 5. Verify MCP tools are used

Ask:

```sh
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is the latest Helidon version?","summary":""}'
```

Expected value now comes from MCP server config:

```text
4.4.0-FROM-MCP-SERVER
```

This confirms the app is using MCP-based tools instead of local tool methods.

---

### Congratulations! You Have Completed the Agentic HOL.

