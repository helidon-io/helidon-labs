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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarkdownArchiveTest {
    private static final String COMMIT = "a".repeat(40);
    private static final String CORE_PATH = "se/helidon/docs/config/Config.md";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void readsBothSeSourcesAndMpWithProvenance() throws Exception {
        var documents = MarkdownArchive.read(archive(manifest(), content()));

        assertThat(documents.size(), is(4));
        var config = documents.getFirst();
        assertThat(config.path(), is(CORE_PATH));
        assertThat(config.text(), is("# Configuration\n\n```yaml\nserver:\n  port: 8080\n```\n"));
        assertThat(config.flavor(), is("SE"));
        assertThat(config.metadata(), is(Map.of("repository", "helidon-io/helidon",
                                               "commit", COMMIT,
                                               "path", "docs/config/Config.md",
                                               "flavor", "SE",
                                               "source", "https://github.com/helidon-io/helidon/blob/" + COMMIT
                                                       + "/docs/config/Config.md")));
        assertThat(documents.get(1).path(), is("se/helidon/docs/includes/example.md"));
        var extension = documents.get(2);
        assertThat(extension.flavor(), is("SE"));
        assertThat(extension.metadata().get("extension"), is("langchain4j"));
        assertThat(extension.metadata().get("source"), is("https://github.com/helidon-io/helidon-extensions/blob/"
                                                                + COMMIT + "/extensions/langchain4j/docs/README.md"));
        assertThat(documents.get(3).flavor(), is("MP"));
        assertThat(documents.get(3).metadata().get("repository"), is("helidon-io/helidon-microprofile"));
    }

    @Test
    void rejectsMissingFile() throws Exception {
        var files = content();
        files.remove(CORE_PATH);
        Path archive = archive(manifest(), files);
        var error = assertThrows(IllegalArgumentException.class, () -> MarkdownArchive.read(archive));
        assertThat(error.getMessage(), containsString("Missing archive entry: " + CORE_PATH));
    }

    @Test
    void rejectsChangedContent() throws Exception {
        var files = content();
        files.put(CORE_PATH, "corrupt".getBytes(StandardCharsets.UTF_8));
        Path archive = archive(manifest(), files);
        var error = assertThrows(IllegalArgumentException.class, () -> MarkdownArchive.read(archive));
        assertThat(error.getMessage(), containsString("checksum or size mismatch"));
    }

    @Test
    void rejectsUnknownSourceReference() throws Exception {
        var manifest = manifest();
        ((ObjectNode) manifest.path("files").get(0)).put("source", "missing");
        Path archive = archive(manifest, content());
        var error = assertThrows(IllegalArgumentException.class, () -> MarkdownArchive.read(archive));
        assertThat(error.getMessage(), containsString("Invalid Markdown file reference"));
    }

    @Test
    void rejectsMismatchedArchivePath() throws Exception {
        var manifest = manifest();
        ((ObjectNode) manifest.path("files").get(0)).put("path", "mp/helidon/docs/config/Config.md");
        Path archive = archive(manifest, content());
        var error = assertThrows(IllegalArgumentException.class, () -> MarkdownArchive.read(archive));
        assertThat(error.getMessage(), containsString("Invalid Markdown file reference"));
    }

    @Test
    void rejectsTraversalInSourcePath() throws Exception {
        var manifest = manifest();
        var file = (ObjectNode) manifest.path("files").get(0);
        file.put("source_path", "docs/../outside.md");
        file.put("path", "se/helidon/docs/../outside.md");
        Path archive = archive(manifest, content());
        var error = assertThrows(IllegalArgumentException.class, () -> MarkdownArchive.read(archive));
        assertThat(error.getMessage(), containsString("Invalid Markdown file reference"));
    }

    @Test
    void rejectsSeSourceClassifiedAsMp() throws Exception {
        var manifest = manifest();
        var source = (ObjectNode) manifest.path("sources").get(0);
        source.put("flavor", "MP");
        source.put("archive_prefix", "mp/helidon");
        Path archive = archive(manifest, content());
        var error = assertThrows(IllegalArgumentException.class, () -> MarkdownArchive.read(archive));
        assertThat(error.getMessage(), containsString("Invalid Markdown source: helidon"));
    }

    private static Map<String, byte[]> content() {
        var content = new LinkedHashMap<String, byte[]>();
        content.put(CORE_PATH, "# Configuration\n\n```yaml\nserver:\n  port: 8080\n```\n".getBytes(StandardCharsets.UTF_8));
        content.put("se/helidon/docs/includes/example.md", "An included example".getBytes(StandardCharsets.UTF_8));
        content.put("se/helidon-extensions/extensions/langchain4j/docs/README.md",
                    "# LangChain4j\n".getBytes(StandardCharsets.UTF_8));
        content.put("mp/helidon-microprofile/docs/config.md", "# MP Configuration\n".getBytes(StandardCharsets.UTF_8));
        return content;
    }

    private static ObjectNode manifest() throws Exception {
        var manifest = MAPPER.createObjectNode().put("format_version", 1);
        var sources = manifest.putArray("sources");
        for (String id : new String[] {"helidon", "helidon-extensions", "helidon-microprofile"}) {
            String flavor = id.equals("helidon-microprofile") ? "MP" : "SE";
            sources.addObject().put("id", id)
                    .put("repository", "helidon-io/" + id)
                    .put("commit", COMMIT)
                    .put("flavor", flavor)
                    .put("source_path", id.equals("helidon-extensions") ? "extensions" : "docs")
                    .put("archive_prefix", flavor.toLowerCase(Locale.ROOT) + "/" + id);
        }
        var files = manifest.putArray("files");
        for (var entry : content().entrySet()) {
            String[] path = entry.getKey().split("/", 3);
            files.addObject().put("path", entry.getKey())
                    .put("source", path[1])
                    .put("source_path", path[2])
                    .put("bytes", entry.getValue().length)
                    .put("sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(entry.getValue())));
        }
        return manifest;
    }

    private Path archive(ObjectNode manifest, Map<String, byte[]> content) throws Exception {
        Path path = tempDir.resolve("docs.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(path))) {
            zip.putNextEntry(new ZipEntry("manifest.json"));
            zip.write(MAPPER.writeValueAsBytes(manifest));
            zip.closeEntry();
            for (var entry : content.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        return path;
    }
}
