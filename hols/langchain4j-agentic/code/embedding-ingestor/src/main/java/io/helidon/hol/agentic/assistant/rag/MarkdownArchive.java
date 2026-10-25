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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class MarkdownArchive {
    private MarkdownArchive() {
    }

    static List<SourceDocument> read(Path zipPath) {
        try (var zip = new ZipFile(zipPath.toFile())) {
            JsonNode manifest = new ObjectMapper().readTree(bytes(zip, "manifest.json"));
            if (manifest == null || manifest.path("format_version").asInt() != 1
                    || !manifest.path("sources").isArray() || !manifest.path("files").isArray()) {
                throw new IllegalArgumentException("Unsupported Markdown archive manifest");
            }
            var sources = new HashMap<String, JsonNode>();
            for (var source : manifest.path("sources")) {
                String id = required(source, "id");
                boolean extensionComponent = id.startsWith("helidon-extensions-");
                String flavor = switch (id) {
                    case "helidon", "helidon-extensions" -> "SE";
                    case "helidon-microprofile" -> "MP";
                    default -> {
                        if (!extensionComponent) {
                            throw new IllegalArgumentException("Unknown Markdown source: " + id);
                        }
                        yield "SE";
                    }
                };
                String repository = "helidon-io/" + id;
                String root = id.equals("helidon-extensions") ? "extensions" : "docs";
                if (extensionComponent) {
                    repository = "helidon-io/helidon-extensions";
                    root = required(source, "source_path");
                    if (!validPath(root) || !root.startsWith("extensions/")
                            || !id.equals("helidon-extensions-" + root.substring("extensions/".length()).replace('/', '-'))) {
                        throw new IllegalArgumentException("Invalid Markdown source: " + id);
                    }
                } else if (id.equals("helidon-microprofile")
                        && source.path("repository").asText().equals("helidon-io/helidon")) {
                    repository = "helidon-io/helidon";
                    root = "docs/mp";
                }
                if (!required(source, "flavor").equals(flavor)
                        || !required(source, "repository").equals(repository)
                        || !required(source, "source_path").equals(root)
                        || !required(source, "archive_prefix").equals(flavor.toLowerCase(Locale.ROOT) + "/" + id)
                        || !required(source, "commit").matches("[0-9a-f]{40}")
                        || sources.putIfAbsent(id, source) != null) {
                    throw new IllegalArgumentException("Invalid Markdown source: " + id);
                }
            }

            var documents = new ArrayList<SourceDocument>();
            var paths = new HashSet<String>();
            for (var file : manifest.path("files")) {
                String path = required(file, "path");
                String sourcePath = required(file, "source_path");
                var source = sources.get(required(file, "source"));
                if (source == null || !validPath(sourcePath) || !sourcePath.endsWith(".md")
                        || !sourcePath.startsWith(source.path("source_path").asText() + "/")
                        || !path.equals(source.path("archive_prefix").asText() + "/" + sourcePath)
                        || !paths.add(path)) {
                    throw new IllegalArgumentException("Invalid Markdown file reference: " + path);
                }
                byte[] content = bytes(zip, path);
                if (!required(file, "sha256").equals(sha256(content))
                        || file.path("bytes").asLong(-1) != content.length) {
                    throw new IllegalArgumentException("Markdown file checksum or size mismatch: " + path);
                }
                String repository = source.path("repository").asText();
                String commit = source.path("commit").asText();
                String flavor = source.path("flavor").asText();
                var metadata = new HashMap<>(Map.of("repository", repository,
                                                   "commit", commit,
                                                   "path", sourcePath,
                                                   "flavor", flavor,
                                                   "source", "https://github.com/" + repository + "/blob/" + commit
                                                           + "/" + sourcePath));
                if (source.path("id").asText().equals("helidon-extensions")) {
                    String[] components = sourcePath.split("/");
                    if (components.length > 2) {
                        metadata.put("extension", components[1]);
                    }
                } else if (source.path("id").asText().startsWith("helidon-extensions-")) {
                    metadata.put("extension", source.path("source_path").asText().substring("extensions/".length()));
                }
                documents.add(new SourceDocument(path, new String(content, StandardCharsets.UTF_8), flavor,
                                                 Map.copyOf(metadata)));
            }
            if (documents.isEmpty()) {
                throw new IllegalArgumentException("Markdown archive contains no documents");
            }
            return List.copyOf(documents);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read Markdown archive: " + zipPath, e);
        }
    }

    private static String required(JsonNode node, String field) {
        var value = node.path(field);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing manifest field: " + field);
        }
        return value.asText();
    }

    private static boolean validPath(String path) {
        return !path.startsWith("/") && !path.contains("\\")
                && List.of(path.split("/", -1)).stream()
                .noneMatch(part -> part.isEmpty() || part.equals(".") || part.equals(".."));
    }

    private static byte[] bytes(ZipFile zip, String path) {
        var entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory()) {
            throw new IllegalArgumentException("Missing archive entry: " + path);
        }
        try (var input = zip.getInputStream(entry)) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read archive entry: " + path, e);
        }
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    record SourceDocument(String path, String text, String flavor, Map<String, String> metadata) {
    }
}
