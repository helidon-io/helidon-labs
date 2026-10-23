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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    static void main() throws IOException {
        var config = Config.create();

        var ingestorConfig = config.get("ingestor");
        var docsZipUri = URI.create(ingestorConfig.get("docs-zip-uri").asString().get());
        var sePath = getPathFromConfig(ingestorConfig, "se-embeddings-path");
        var mpPath = getPathFromConfig(ingestorConfig, "mp-embeddings-path");
        var allPath = getPathFromConfig(ingestorConfig, "all-embeddings-path");
        var embeddingsZipPath = getPathFromConfig(ingestorConfig, "embeddings-zip-path");

        var docsZipPath = Files.createTempFile("helidon-markdown-docs-", ".zip");
        try {
            downloadArchive(docsZipUri, docsZipPath);
            System.out.println("Ingesting " + docsZipPath);

            var seStore = new InMemoryEmbeddingStore<TextSegment>();
            var mpStore = new InMemoryEmbeddingStore<TextSegment>();
            var embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

            var ingestor = new DocsIngestor(docsZipPath, seStore, mpStore, embeddingModel);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var ingestFuture = CompletableFuture.runAsync(ingestor::ingestAll, executor);
                renderProgressUntilDone(ingestor, ingestFuture);
                ingestFuture.join();
            }
            var allStore = InMemoryEmbeddingStore.merge(seStore, mpStore);

            System.out.println();
            serializeToFile(seStore, sePath);
            serializeToFile(mpStore, mpPath);
            serializeToFile(allStore, allPath);
            zipEmbeddings(embeddingsZipPath, sePath, mpPath, allPath);

            System.out.printf(
                    """
                            Embeddings serialized successfully.
                            SE  -> %s
                            MP  -> %s
                            ALL -> %s
                            ZIP -> %s
                            Total files processed: %d
                            """,
                    sePath, mpPath, allPath, embeddingsZipPath, ingestor.progressTotal()
            );
        } finally {
            Files.deleteIfExists(docsZipPath);
        }
    }

    private static Path getPathFromConfig(Config config, String key) {
        return config.get(key)
                .as(Path.class)
                .orElseThrow(() -> new ConfigException("Missing ingestor." + key));
    }

    private static void downloadArchive(URI uri, Path destination) throws IOException {
        System.out.println("Downloading " + uri);
        try (var input = uri.toURL().openStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void serializeToFile(InMemoryEmbeddingStore<TextSegment> store, Path jsonPath) throws IOException {
        Files.createDirectories(jsonPath.toAbsolutePath().getParent());
        store.serializeToFile(jsonPath);
    }

    private static void zipEmbeddings(Path destination, Path sePath, Path mpPath, Path allPath) throws IOException {
        Files.createDirectories(destination.toAbsolutePath().getParent());
        try (var zip = new ZipOutputStream(Files.newOutputStream(destination))) {
            var paths = new Path[]{sePath, mpPath, allPath};
            var names = new String[]{"se-embeddings.json", "mp-embeddings.json", "all-embeddings.json"};
            for (int i = 0; i < paths.length; i++) {
                zip.putNextEntry(new ZipEntry(names[i]));
                Files.copy(paths[i], zip);
                zip.closeEntry();
            }
        }
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
