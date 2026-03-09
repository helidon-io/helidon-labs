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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;

public class DocsIngestor {
    private static final boolean DEBUG = Boolean.getBoolean("ingestor.debug");

    private final Path docsZipPath;
    private final InMemoryEmbeddingStore<TextSegment> seEmbeddingStore;
    private final InMemoryEmbeddingStore<TextSegment> mpEmbeddingStore;
    private final EmbeddingModel embeddingModel;

    private final LongAdder progressTotal = new LongAdder();
    private final LongAdder progressRemaining = new LongAdder();

    public DocsIngestor(Path docsZipPath,
                        InMemoryEmbeddingStore<TextSegment> seEmbeddingStore,
                        InMemoryEmbeddingStore<TextSegment> mpEmbeddingStore,
                        EmbeddingModel embeddingModel) {
        this.docsZipPath = docsZipPath;
        this.seEmbeddingStore = seEmbeddingStore;
        this.mpEmbeddingStore = mpEmbeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public void ingestAll() {
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            allOf(
                    runAsync(() -> ingest(HelidonFlavor.MP), ex),
                    runAsync(() -> ingest(HelidonFlavor.SE), ex)
            ).join();
        }
    }

    public long progressTotal() {
        return progressTotal.longValue();
    }

    public long progressRemaining() {
        return progressRemaining.longValue();
    }

    void ingest(HelidonFlavor flavor) {
        EmbeddingStore<TextSegment> embeddingStore = switch (flavor) {
            case SE -> seEmbeddingStore;
            case MP -> mpEmbeddingStore;
        };

        var root = AsciiFileLister.unzip(docsZipPath);
        var files = AsciiFileLister.listFiles(root.resolve(flavor.name().toLowerCase()).toAbsolutePath());

        progressTotal.add(files.size());
        progressRemaining.add(files.size());

        var processor = new AsciiDocPreprocessor();
        for (Path path : files) {
            var chunks = processor.extractChunks(path.toFile(), root, flavor);
            var groupedChunks = groupChunks(chunks, 1000);

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

            var embeddings = embeddingModel.embedAll(segments);

            if (DEBUG) {
                for (int i = 0; i < segments.size(); i++) {
                    TextSegment segment = segments.get(i);
                    System.out.printf("Chunk %d:%n%s%n%n", i + 1, segment.text());
                    System.out.printf("Metadata: %s%n", segment.metadata());
                    System.out.printf("Embedding vector size: %d%n", embeddings.content().get(i).vector().length);
                    System.out.println("---");
                }
            }

            embeddingStore.addAll(embeddings.content(), segments);
            progressRemaining.decrement();
        }
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

    enum HelidonFlavor {
        MP, SE;
    }
}
