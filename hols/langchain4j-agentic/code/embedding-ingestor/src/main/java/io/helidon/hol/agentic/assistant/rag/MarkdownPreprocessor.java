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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListBlock;
import org.commonmark.node.Paragraph;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;

final class MarkdownPreprocessor {
    private static final int MAX_CHUNK_LENGTH = 1000;

    private final Parser parser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();

    List<Chunk> extractChunks(String markdown) {
        // YAML frontmatter; Helidon's HTML comment frontmatter is removed by HTML text extraction.
        markdown = markdown.replaceFirst("(?s)\\A\\uFEFF?---\\R(?=[\\w-]+:).*?\\R---(?:\\R|$)", "");
        var chunks = new ArrayList<Chunk>();
        var headings = new String[6];
        var document = parser.parse(markdown);
        for (var node = document.getFirstChild(); node != null; node = node.getNext()) {
            var html = Jsoup.parseBodyFragment(renderer.render(node));
            html.select("wbr, script, style").remove();
            for (var image : html.select("img[alt]")) {
                image.replaceWith(new TextNode(image.attr("alt")));
            }
            if (node instanceof Heading heading) {
                headings[heading.getLevel() - 1] = html.body().text();
                Arrays.fill(headings, heading.getLevel(), headings.length, null);
                continue;
            }
            if (node instanceof ThematicBreak || node instanceof LinkReferenceDefinition) {
                continue;
            }

            Chunk.Type type;
            String text;
            if (node instanceof FencedCodeBlock code) {
                type = Chunk.Type.CODE;
                text = code.getLiteral();
            } else if (node instanceof IndentedCodeBlock code) {
                type = Chunk.Type.CODE;
                text = code.getLiteral();
            } else {
                if (node instanceof TableBlock || !html.select("table").isEmpty()) {
                    type = Chunk.Type.TABLE;
                    for (var row : html.select("tr")) {
                        String cells = row.children().stream()
                                .map(cell -> cell.text())
                                .collect(Collectors.joining(" | "));
                        row.replaceWith(new TextNode(cells + "\n"));
                    }
                } else {
                    type = node instanceof ListBlock ? Chunk.Type.LIST
                            : node instanceof Paragraph ? Chunk.Type.PARAGRAPH : Chunk.Type.MIXED;
                }
                // Comments (including site directives and include markers) contribute no text.
                text = html.body().wholeText().replaceAll("\\n[\\t ]*\\n+", "\n\n").strip();
            }
            String sectionPath = Arrays.stream(headings)
                    .filter(heading -> heading != null && !heading.isBlank())
                    .collect(Collectors.joining(" > "));
            addChunks(chunks, text, type, sectionPath);
        }
        return chunks;
    }

    private static void addChunks(List<Chunk> chunks, String text, Chunk.Type type, String sectionPath) {
        // Prefer complete lines, then words; even a very long code line stays within the embedding budget.
        for (int start = 0; start < text.length();) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, text.length());
            if (end < text.length()) {
                int boundary = text.lastIndexOf('\n', end - 1);
                if (boundary < start + MAX_CHUNK_LENGTH / 2) {
                    boundary = text.lastIndexOf(' ', end - 1);
                }
                if (boundary >= start + MAX_CHUNK_LENGTH / 2) {
                    end = boundary + 1;
                }
                if (Character.isHighSurrogate(text.charAt(end - 1))) {
                    end--;
                }
            }
            String part = text.substring(start, end);
            if (!part.isBlank()) {
                chunks.add(new Chunk(part, type, sectionPath));
            }
            start = end;
        }
    }

    record Chunk(String text, Type type, String sectionPath) {
        enum Type {PARAGRAPH, CODE, TABLE, LIST, MIXED}
    }
}
