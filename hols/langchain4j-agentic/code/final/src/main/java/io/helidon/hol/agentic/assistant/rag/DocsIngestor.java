/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.hol.agentic.assistant.rag;

import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import io.helidon.common.features.api.HelidonFlavor;
import io.helidon.config.Config;
import io.helidon.config.ConfigException;
import io.helidon.hol.agentic.assistant.dto.IngestionProgress;
import io.helidon.service.registry.Service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;

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
