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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        this.docsZipPath = Objects.requireNonNull(docsZipPath);
        this.seEmbeddingStore = Objects.requireNonNull(seEmbeddingStore);
        this.mpEmbeddingStore = Objects.requireNonNull(mpEmbeddingStore);
        this.embeddingModel = Objects.requireNonNull(embeddingModel);
    }

    public void ingestAll() {
        var documents = MarkdownArchive.read(docsZipPath);
        progressTotal.add(documents.size());
        progressRemaining.add(documents.size());
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            allOf(
                    runAsync(() -> ingest(HelidonFlavor.MP, documents), ex),
                    runAsync(() -> ingest(HelidonFlavor.SE, documents), ex)
            ).join();
        }
    }

    public long progressTotal() {
        return progressTotal.longValue();
    }

    public long progressRemaining() {
        return progressRemaining.longValue();
    }

    private static List<MarkdownPreprocessor.Chunk> groupChunks(List<MarkdownPreprocessor.Chunk> input, int maxChars) {
        var grouped = new ArrayList<MarkdownPreprocessor.Chunk>();
        var builder = new StringBuilder();
        String currentSection = "";
        var type = MarkdownPreprocessor.Chunk.Type.MIXED;
        for (var chunk : input) {
            if (!chunk.sectionPath().equals(currentSection) || builder.length() + chunk.text().length() + 2 > maxChars) {
                if (!builder.isEmpty()) {
                    grouped.add(new MarkdownPreprocessor.Chunk(builder.toString().trim(), type, currentSection));
                    builder.setLength(0);
                }
            }
            currentSection = chunk.sectionPath();
            builder.append(chunk.text()).append("\n\n");
        }
        if (!builder.isEmpty()) {
            grouped.add(new MarkdownPreprocessor.Chunk(builder.toString().trim(), type, currentSection));
        }
        return grouped;
    }

    private void ingest(HelidonFlavor flavor, List<MarkdownArchive.SourceDocument> documents) {
        EmbeddingStore<TextSegment> embeddingStore = switch (flavor) {
            case SE -> seEmbeddingStore;
            case MP -> mpEmbeddingStore;
        };

        var processor = new MarkdownPreprocessor();
        for (var document : documents) {
            if (!document.flavor().equals(flavor.name())) {
                continue;
            }
            var chunks = processor.extractChunks(document.text());
            var groupedChunks = groupChunks(chunks, 1000);

            List<TextSegment> segments = new ArrayList<>();
            for (int i = 0; i < groupedChunks.size(); i++) {
                var chunk = groupedChunks.get(i);
                var metadata = new Metadata(document.metadata())
                        .put("chunk", String.valueOf(i + 1))
                        .put("type", chunk.type().name())
                        .put("section", chunk.sectionPath());

                String text = chunk.sectionPath().isEmpty()
                        ? chunk.text()
                        : chunk.sectionPath() + "\n\n" + chunk.text();
                segments.add(TextSegment.from(text, metadata));
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

    enum HelidonFlavor {
        MP, SE;
    }
}
