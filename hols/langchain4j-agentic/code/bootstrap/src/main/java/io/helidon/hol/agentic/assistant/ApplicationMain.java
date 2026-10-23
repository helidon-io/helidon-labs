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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipInputStream;

import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;

@Service.GenerateBinding
public class ApplicationMain {
    static void main() throws IOException {
        LogConfig.configureRuntime();
        downloadEmbeddingsFile();
        // Start Helidon
        ServiceRegistryManager.start(ApplicationBinding.create());
        System.out.println("Helidon Assistant is running at http://localhost:8080");
    }

    private static void downloadEmbeddingsFile() throws IOException {
        var missing = new HashSet<>(List.of("all-embeddings.json", "se-embeddings.json", "mp-embeddings.json"));
        missing.removeIf(name -> Files.exists(Path.of(name)));
        if (missing.isEmpty()) {
            return;
        }
        System.out.println("Downloading embeddings.zip...");
        try (var input = new ZipInputStream(new BufferedInputStream(
                URI.create("https://objectstorage.us-chicago-1.oraclecloud.com/n/axh6g42yikfw/b/lc4j-agents-lab/o/embeddings.zip")
                        .toURL().openStream()))) {
            for (var entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                if (!missing.remove(entry.getName())) {
                    continue;
                }
                Path temporaryFile = Files.createTempFile(Path.of("."), ".embeddings-", ".tmp");
                try {
                    Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
                    Files.move(temporaryFile, Path.of(entry.getName()));
                    System.out.println("Extracted " + entry.getName());
                } finally {
                    Files.deleteIfExists(temporaryFile);
                }
            }
        }
    }
}
