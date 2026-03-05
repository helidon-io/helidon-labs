# 6. Wiring RAG and REST Endpoint

In this section, we will:

- Route ingestion into separate embedding stores for SE and MP.
- Switch REST endpoint wiring from `HelidonExpert` to `HelidonExpertAgent`.
- Update the page title to match the final app.

---

## 1. Replace `DocsIngestor.java`

Replace:

`src/main/java/io/helidon/hol/agentic/assistant/rag/DocsIngestor.java`

with:

```java
package io.helidon.hol.agentic.assistant.rag;

@Service.Singleton
@Service.RunLevel(1)
public class DocsIngestor {
    private static final Logger LOGGER = System.getLogger(DocsIngestor.class.getName());

    private final Config config;
    private final EmbeddingStore<TextSegment> seEmbeddingStore;
    private final EmbeddingStore<TextSegment> mpEmbeddingStore;
    private final EmbeddingModel embeddingModel;

    private final LongAdder progressTotal = new LongAdder();
    private final LongAdder progressRemaining = new LongAdder();

    @Service.PostConstruct
    void onCreate() {
        // Initialize embedding store
        this.ingestAll();
    }

    @Service.Inject
    DocsIngestor(Config config,
                 @Service.Named("se-embedding-store") EmbeddingStore<TextSegment> seEmbeddingStore,
                 @Service.Named("mp-embedding-store") EmbeddingStore<TextSegment> mpEmbeddingStore,
                 @Service.Named("assistant-embedding-model") EmbeddingModel embeddingModel) {
        this.config = config;
        this.seEmbeddingStore = seEmbeddingStore;
        this.mpEmbeddingStore = mpEmbeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public void ingestAll() {
        LOGGER.log(INFO, "Starting ingestion ...");
        var ex = Executors.newVirtualThreadPerTaskExecutor();
        allOf(
                runAsync(() -> ingest(HelidonFlavor.MP), ex),
                runAsync(() -> ingest(HelidonFlavor.SE), ex)
        );
    }

    void ingest(HelidonFlavor flavor) {
        var embeddingStore = switch (flavor) {
            case SE -> seEmbeddingStore;
            case MP -> mpEmbeddingStore;
        };

        // Get files to process
        var appConfig = config.get("app");
        var zipDirPath = appConfig.get("docs-zip-path")
                .as(Path.class)
                .orElseThrow(() -> new ConfigException("Missing app.docs-zip-path property with path to Helidon project "
                                                               + "dir"));

        var root = AsciiFileLister.unzip(zipDirPath);

        var files = AsciiFileLister.listFiles(root.resolve(flavor.name().toLowerCase()).toAbsolutePath());

        LOGGER.log(INFO, "Ingesting {0} {1} files", files.size(), flavor.name());

        progressTotal.add(files.size());
        progressRemaining.add(files.size());

        // Process files
        var processor = new AsciiDocPreprocessor();
        for (Path path : files) {
            var chunks = processor.extractChunks(path.toFile(), root, flavor);
            var groupedChunks = groupChunks(chunks, 1000);

            // Convert to LangChain4J TextSegments with metadata
            List<TextSegment> segments = new ArrayList<>();
            for (int i = 0; i < groupedChunks.size(); i++) {
                var chunk = groupedChunks.get(i);
                var metadata = new Metadata()
                        .put("source", path.toFile().getAbsolutePath())
                        .put("chunk", String.valueOf(i + 1))
                        .put("type", chunk.type().name())
                        .put("section", chunk.sectionPath());

                segments.add(TextSegment.from(chunk.text(), metadata));
            }

            if (segments.isEmpty()) {
                progressRemaining.decrement();
                continue;
            }

            // Embed the segments
            var embeddings = embeddingModel.embedAll(segments);

            if (LOGGER.isLoggable(DEBUG)) {
                // Print segments and metadata
                for (int i = 0; i < segments.size(); i++) {
                    TextSegment segment = segments.get(i);
                    LOGGER.log(DEBUG, "Chunk {0}:\n{1}\n", i + 1, segment.text());
                    LOGGER.log(DEBUG, "Metadata: {0}", segment.metadata());
                    LOGGER.log(DEBUG, "Embedding vector size: {0}", embeddings.content().get(i).vector().length);
                    LOGGER.log(DEBUG, "---");
                }
            }

            embeddingStore.addAll(embeddings.content(), segments);
            progressRemaining.decrement();
        }

        LOGGER.log(INFO, "Ingestion done for {0}", flavor.name());
    }

    public IngestionProgress progress() {
        return new IngestionProgress(progressTotal.longValue(), progressRemaining.longValue());
    }

    private static List<AsciiDocPreprocessor.Chunk> groupChunks(List<AsciiDocPreprocessor.Chunk> input, int maxChars) {
        var grouped = new ArrayList<AsciiDocPreprocessor.Chunk>();
        var builder = new StringBuilder();
        String currentSection = null;
        var type = AsciiDocPreprocessor.Chunk.Type.MIXED;
        for (var chunk : input) {
            if (currentSection == null) {
                currentSection = chunk.sectionPath();
            }
            // If switching section or chunk is too big to add
            if (!chunk.sectionPath().equals(currentSection) || builder.length() + chunk.text().length() > maxChars) {
                if (!builder.isEmpty()) {
                    grouped.add(new AsciiDocPreprocessor.Chunk(builder.toString().trim(), type, currentSection));
                    builder.setLength(0);
                }
                currentSection = chunk.sectionPath();
            }
            builder.append(chunk.text()).append("\n\n");
        }
        if (!builder.isEmpty()) {
            grouped.add(new AsciiDocPreprocessor.Chunk(builder.toString().trim(), type, currentSection));
        }
        return grouped;
    }
}
```

## 2. Replace `ChatBotEndpoint.java`

Replace:

`src/main/java/io/helidon/hol/agentic/assistant/rest/ChatBotEndpoint.java`

with:

```java
package io.helidon.hol.agentic.assistant.rest;

import io.helidon.hol.agentic.assistant.ai.HelidonExpertAgent;
import io.helidon.hol.agentic.assistant.dto.ExpertMessage;
import io.helidon.hol.agentic.assistant.dto.IngestionProgress;
import io.helidon.hol.agentic.assistant.rag.DocsIngestor;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

import static io.helidon.common.media.type.MediaTypes.APPLICATION_JSON_VALUE;

@RestServer.Endpoint
@Http.Path
@Service.Singleton
class ChatBotEndpoint {

    private final HelidonExpertAgent agent;
    private final DocsIngestor ingestor;

    @Service.Inject
    ChatBotEndpoint(HelidonExpertAgent agent, DocsIngestor ingestor) {
        this.agent = agent;
        this.ingestor = ingestor;
    }

    @Http.GET
    @Http.Path("/progress")
    @Http.Produces(APPLICATION_JSON_VALUE)
    IngestionProgress ingestionProgress() {
        return ingestor.progress();
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

