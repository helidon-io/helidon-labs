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

package io.helidon.hol.agentic.assistant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import io.helidon.config.Config;
import io.helidon.config.ConfigException;
import io.helidon.hol.agentic.assistant.rag.DocsIngestor;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public final class EmbeddingIngestorMain {
    private static final int PROGRESS_BAR_WIDTH = 36;

    private EmbeddingIngestorMain() {
    }

    public static void main(String[] args) {
        var config = Config.create();

        var ingestorConfig = config.get("ingestor");
        var docsZipPath = getPathFromConfig(ingestorConfig, "docs-zip-path");
        var sePath = getPathFromConfig(ingestorConfig, "se-embeddings-path");
        var mpPath = getPathFromConfig(ingestorConfig, "mp-embeddings-path");
        var allPath = getPathFromConfig(ingestorConfig, "all-embeddings-path");

        if (Files.notExists(docsZipPath)) {
            System.out.println("ERROR: file " + docsZipPath + " does not exist. Skipping ingestion.");
            System.exit(1);
        }

        System.out.println("Ingesting " + docsZipPath);

        var seStore = new InMemoryEmbeddingStore<TextSegment>();
        var mpStore = new InMemoryEmbeddingStore<TextSegment>();
        var embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

        var ingestor = new DocsIngestor(docsZipPath, seStore, mpStore, embeddingModel);
        var ingestFuture = CompletableFuture.runAsync(ingestor::ingestAll,
                                                      Executors.newVirtualThreadPerTaskExecutor());
        renderProgressUntilDone(ingestor, ingestFuture);
        ingestFuture.join();
        var allStore = InMemoryEmbeddingStore.merge(seStore, mpStore);

        System.out.println();
        serializeToFile(seStore, sePath);
        serializeToFile(mpStore, mpPath);
        serializeToFile(allStore, allPath);

        System.out.printf(
                """
                        Embeddings serialized successfully.
                        SE  -> %s
                        MP  -> %s
                        ALL -> %s
                        Total files processed: %d
                        """,
                sePath, mpPath, allPath, ingestor.progressTotal()
        );
    }

    private static Path getPathFromConfig(Config config, String key) {
        return config.get(key)
                .as(Path.class)
                .orElseThrow(() -> new ConfigException("Missing ingestor." + key));
    }

    private static void serializeToFile(InMemoryEmbeddingStore<TextSegment> store, Path jsonPath) {
        try {
            Files.createDirectories(jsonPath.toAbsolutePath().getParent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare output directories", e);
        }

        store.serializeToFile(jsonPath);
    }

    private static void renderProgressUntilDone(DocsIngestor ingestor, CompletableFuture<Void> ingestFuture) {
        while (!ingestFuture.isDone()) {
            long total = ingestor.progressTotal();
            long remaining = ingestor.progressRemaining();
            long processed = Math.max(0, total - remaining);

            if (total > 0) {
                double ratio = Math.clamp((double) processed / (double) total, 0.0, 1.0);
                int filled = (int) Math.round(ratio * PROGRESS_BAR_WIDTH);
                String bar = "#".repeat(filled) + "-".repeat(PROGRESS_BAR_WIDTH - filled);
                System.out.printf("\r[%s] %6.2f%% (%d/%d files)", bar, ratio * 100.0, processed, total);
                System.out.flush();
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        long total = ingestor.progressTotal();
        if (total > 0) {
            String bar = "#".repeat(PROGRESS_BAR_WIDTH);
            System.out.printf("\r[%s] %6.2f%% (%d/%d files)", bar, 100.0, total, total);
            System.out.flush();
        }
    }
}
