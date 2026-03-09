# 3. Configuring the Project for Agentic AI

In this section, we will:

- Configure separate models, embedding stores, and retrievers for SE and MP flows.

---

## 1. Replace `src/main/resources/application.yaml`

Replace the file with:

```yaml
server:
  host: "0.0.0.0"
  port: 8080
  features:
    static-content:
      classpath:
        - context: "/"
          location: "/WEB"
          welcome: "index.html"

declarative:
  ignore-incubating: true;

langchain4j:
  providers:
    lc4j-in-process:
      type: all_minilm_l6_v2_q

    open-ai:
      # Using OpenAI provider to call OCI GenAI model over OpenAI compatible API
      api-key: "${OCI_API_KEY}"
      base-url: https://inference.generativeai.us-chicago-1.oci.oraclecloud.com/20231130/actions
      log-requests: false
      log-responses: false

    lc4j-content-retriever:
      embedding-model: assistant-embedding-model
      max-results: 20
      min-score: 0.6

  models:
    assistant-embedding-model:
      provider: lc4j-in-process

    cheap-model:
      provider: open-ai
      model-name: "meta.llama-4-scout-17b-16e-instruct"

    expensive-model:
      provider: open-ai
      model-name: "openai.gpt-oss-120b"

  embedding-stores:
    se-embedding-store:
      provider: lc4j-in-memory
    mp-embedding-store:
      provider: lc4j-in-memory

  content-retrievers:
    se-content-retriever:
      provider: lc4j-content-retriever
      embedding-store: se-embedding-store

    mp-content-retriever:
      provider: lc4j-content-retriever
      embedding-store: mp-embedding-store

app:
  latest-helidon-version: 4.4.0-FROM-TOOL
```

## 2. Understand the YAML Configuration

Helidon LangChain4j uses a **unified configuration model**:

- `langchain4j.providers`: shared provider-level defaults and connection settings.
- `langchain4j.models`, `embedding-stores`, `content-retrievers`, `mcp-clients`: named components that become named singleton services in Helidon.
- Named components can reuse provider config and override specific values.

### `server`

- `host: "0.0.0.0"` exposes the app on all network interfaces.
- `port: 8080` sets the HTTP server port.
- `features.static-content.classpath` serves the UI from `/WEB`, with `index.html` as welcome page.

### `declarative`

- `ignore-incubating: true;` allows the app to use incubating declarative features without startup failure.

### `langchain4j.providers`

This section defines reusable provider settings.

- `lc4j-in-process`
  - `type: all_minilm_l6_v2_q` configures a local in-process embedding model provider.
  - Used by `assistant-embedding-model`.

- `open-ai`
  - Configures OpenAI-compatible access (here pointed to OCI Generative AI endpoint).
  - `api-key: "${OCI_API_KEY}"` reads token from environment variable.
  - `base-url` points requests to OCI OpenAI-compatible API.
  - `log-requests` and `log-responses` control wire logging.

- `lc4j-content-retriever`
  - Defines defaults for embedding-based retrieval.
  - `embedding-model: assistant-embedding-model` selects which embedding model is used for query embedding.
  - `max-results` limits retrieved chunks per query.
  - `min-score` filters low-similarity matches.

### `langchain4j.models`

Models are named services you can reference from agents/services via `@Ai.ChatModel("name")` or related annotations.

- `assistant-embedding-model`
  - `provider: lc4j-in-process`
  - Local embedding model used by retrievers and ingestion.

- `cheap-model`
  - `provider: open-ai`
  - Lower-cost model used for lightweight tasks (for example classification/summarization in later steps).

- `expensive-model`
  - `provider: open-ai`
  - Higher-capability model used for expert answers.

### `langchain4j.embedding-stores`

- `se-embedding-store` and `mp-embedding-store` use `lc4j-in-memory`.
- These stores are separate so SE and MP documentation embeddings are isolated.

### `langchain4j.content-retrievers`

Each retriever is a named component bound to one embedding store:

- `se-content-retriever` -> `se-embedding-store`
- `mp-content-retriever` -> `mp-embedding-store`

This is what enables flavor-specific RAG in expert agents.

### `app`

This is an application-specific configuration consumed by local services/tools:

- `latest-helidon-version` is used by the helper tool logic in later steps.

### About MCP Client Configuration

In this step we keep local tools enabled.
In the bonus step, you will add `langchain4j.mcp-clients` and switch expert agents from `@Ai.Tools` to `@Ai.McpClients`.



---

### Next Step -> [Creating the Orchestrator Agent](04_creating_the_orchestrator_agent.md)
