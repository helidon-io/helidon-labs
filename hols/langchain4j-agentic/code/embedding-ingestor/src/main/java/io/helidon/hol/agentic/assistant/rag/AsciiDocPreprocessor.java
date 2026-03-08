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
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import io.helidon.hol.agentic.assistant.rag.DocsIngestor.HelidonFlavor;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

class AsciiDocPreprocessor {

    private final Asciidoctor asciidoctor;

    AsciiDocPreprocessor() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }

    List<Chunk> extractChunks(File adocFile, Path rootDir, HelidonFlavor flavor) {
        var flavorLc = flavor.name().toLowerCase();
        var flavorUc = flavor.name().toUpperCase();
        var rootDirPath = rootDir.toAbsolutePath();
        var options = Options.builder()
                .baseDir(adocFile.getParentFile()) // enables include:: to resolve
                .attributes(Attributes.builder()
                                    .attribute("sourcedir", rootDir.getParent()
                                            .resolve(Path.of("java", "io", "helidon", "docs")).toAbsolutePath().toString())
                                    .attribute("flavor-lc", flavorLc)
                                    .attribute("flavor-uc", flavorUc)
                                    .attribute("health-page",
                                               String.format("%s/%s/health.adoc", rootDirPath, flavorLc))
                                    .attribute("metrics-page",
                                               String.format("%s/%s/metrics/metrics.adoc", rootDirPath, flavorLc))
                                    .attribute("openapi-page",
                                               String.format("%s/%s/openapi/openapi.adoc", rootDirPath, flavorLc))
                                    .attribute("tracing-page",
                                               String.format("%s/%s/tracing.adoc", rootDirPath, flavorLc))
                                    .build())
                .safe(SafeMode.UNSAFE)             // allows full access (use cautiously)
                .build();

        var doc = asciidoctor.loadFile(adocFile, options);
        var chunks = new ArrayList<Chunk>();
        walk(doc, chunks, new ArrayDeque<>());
        return chunks;
    }

    private void walk(StructuralNode node, List<Chunk> chunks, Deque<String> sectionStack) {
        if (node instanceof Section) {
            sectionStack.push(((Section) node).getTitle());
        }

        var context = node.getContext();
        var content = node.getContent();
        var sectionPath = joinSection(sectionStack);

        switch (context) {
            case "paragraph":
                chunks.add(new Chunk(strip(content.toString()), Chunk.Type.PARAGRAPH, sectionPath));
                break;
            case "listing":
                chunks.add(new Chunk("// code:\n" + content, Chunk.Type.CODE, sectionPath));
                break;
            case "table":
                chunks.add(new Chunk(tableToText((Table) node), Chunk.Type.TABLE, sectionPath));
                break;
            case "ulist":
            case "olist":
                chunks.add(new Chunk(listToText(node), Chunk.Type.LIST, sectionPath));
                break;
        }

        for (var child : node.getBlocks()) {
            walk(child, chunks, sectionStack);
        }

        if (node instanceof Section) {
            sectionStack.pop();
        }
    }

    private String joinSection(Deque<String> sections) {
        var list = new ArrayList<>(sections);
        Collections.reverse(list);
        return list.isEmpty() ? "" : String.join(" > ", list);
    }

    private String strip(String text) {
        return text.replaceAll("\\*|_+|\\[.+?\\]|`+", "").trim();
    }

    private String tableToText(Table table) {
        var sb = new StringBuilder();
        for (var row : table.getBody()) {
            var cols = new ArrayList<String>();
            for (var cell : row.getCells()) {
                cols.add(cell.getText());
            }
            sb.append(String.join(" | ", cols)).append("\n");
        }
        return sb.toString().trim();
    }

    private String listToText(StructuralNode listNode) {
        var sb = new StringBuilder();

        var list = (org.asciidoctor.ast.List) listNode;
        for (var item: list.getItems()) {
            var listItem = (ListItem) item;
            sb.append("- ").append(strip(listItem.getText())).append("\n");
        }
        return sb.toString().trim();
    }

    record Chunk(String text, Type type, String sectionPath) {
        enum Type {SECTION, PARAGRAPH, CODE, TABLE, LIST, MIXED}
    }
}
