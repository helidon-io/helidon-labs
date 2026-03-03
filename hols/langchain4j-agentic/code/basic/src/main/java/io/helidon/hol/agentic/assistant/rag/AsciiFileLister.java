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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class AsciiFileLister {

    static Path unzip(Path zipFilePath){
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("helidon-docs-unzipped");
            try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
                zipFile.stream().forEach(entry -> {
                    try {
                        if (!entry.isDirectory()) {
                            Path entryDestination = tempDir.resolve(entry.getName());
                            Files.createDirectories(entryDestination.getParent());
                            Files.copy(zipFile.getInputStream(entry), entryDestination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Error extracting zip entry", e);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to uncompress the zip file", e);
        }

        return tempDir.resolve(Path.of("helidon-docs", "src", "main", "asciidoc"));
    }

    static List<Path> listFiles(Path rootDir) {
        var files = new ArrayList<Path>();
        var root = rootDir.toFile();
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("The specified path is not a directory: " + rootDir);
        }
        listFilesRecursive(root, rootDir, files);
        return files;
    }

    private static void listFilesRecursive(File directory, Path rootPath, List<Path> files) {
        var fileList = directory.listFiles();
        if (fileList == null) {
            return; // empty or inaccessible dir
        }
        for (var file : fileList) {
            var relativePath = rootPath.relativize(file.toPath());
            var relativePathString = relativePath.toString();
            if (relativePathString.endsWith(".adoc")) {
                files.add(file.toPath());
            }
            if (file.isDirectory()) {
                listFilesRecursive(file, rootPath, files);
            }
        }
    }
}
