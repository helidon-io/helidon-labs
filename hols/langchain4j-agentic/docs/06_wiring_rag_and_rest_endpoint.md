# 6. Wiring RAG and REST Endpoint

In this section, we will:

- Load persisted SE/MP embeddings from JSON files into in-memory stores.
- Switch REST endpoint wiring from `HelidonExpert` to `HelidonExpertAgent`.
- Update the page title to match the final app.

---

## 1. Update `application.yaml` embedding stores to use `from-file`

Edit:

`src/main/resources/application.yaml`

and update the `langchain4j.embedding-stores` block to:

```yaml
langchain4j:
  embedding-stores:
    se-embedding-store:
      provider: lc4j-in-memory
      from-file: ../../data/se-embeddings.json
    mp-embedding-store:
      provider: lc4j-in-memory
      from-file: ../../data/mp-embeddings.json
```

What these persisted JSON files are:

- They are serialized snapshots of LangChain4j `InMemoryEmbeddingStore` data generated earlier by `embedding-ingestor` (step `2`).
- Each file contains precomputed embedding vectors together with their text segments and metadata.
- `se-embeddings.json` contains only Helidon SE knowledge; `mp-embeddings.json` contains only Helidon MP knowledge.

How `from-file` works here:

- `provider: lc4j-in-memory` still creates an in-memory store at runtime.
- `from-file` tells Helidon/LangChain4j to initialize that in-memory store from the JSON snapshot instead of an empty state.
- After loading, retrievers query these in-memory vectors immediately, so RAG is ready without running document embedding in the app project.

Important:

- Paths in `from-file` are relative to the running application module (for example `code/bootstrap`).
- Make sure step `2` completed and both JSON files exist in `hols/langchain4j-agentic/data`.

## 2. Replace `ChatBotEndpoint.java`

Replace:

`src/main/java/io/helidon/hol/agentic/assistant/rest/ChatBotEndpoint.java`

with:

```java
package io.helidon.hol.agentic.assistant.rest;

import io.helidon.hol.agentic.assistant.ai.HelidonExpertAgent;
import io.helidon.hol.agentic.assistant.dto.ExpertMessage;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

import static io.helidon.common.media.type.MediaTypes.APPLICATION_JSON_VALUE;

@RestServer.Endpoint
@Http.Path
@Service.Singleton
class ChatBotEndpoint {

    private final HelidonExpertAgent agent;

    @Service.Inject
    ChatBotEndpoint(HelidonExpertAgent agent) {
        this.agent = agent;
    }

    @Http.POST
    @Http.Path("/chat")
    @Http.Produces(APPLICATION_JSON_VALUE)
    ExpertMessage chatWithAssistant(@Http.Entity ExpertMessage msg) {
        return agent.chat(msg.message(), msg.summary());
    }
}
```

## 3. Update page title

Edit `src/main/resources/WEB/index.html`:

```html
<h1 class="title">Helidon Agentic Assistant</h1>
```

---

### Next Step -> [Building and Running the Final Assistant](07_building_and_running_the_final_assistant.md)
