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

These JSON files are produced in step `2` by `embedding-ingestor` and then reused by the app.

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
