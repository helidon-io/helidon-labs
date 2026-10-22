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

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class MarkdownPreprocessorTest {
    private final MarkdownPreprocessor preprocessor = new MarkdownPreprocessor();

    @Test
    void preservesHeadingContextAndCode() {
        var chunks = preprocessor.extractChunks("""
                # WebServer
                ## Configuration
                Use `MY_CONFIG` and `${server.port}`.

                ```java [Example.java]
                Map<String, List<Integer>> values = new HashMap<>();
                // Keep MY_CONFIG and ${server.port}
                ```

                ## Routing
                Register routes.
                """);
        assertThat(chunks.size(), is(3));
        assertThat(chunks.getFirst().text(), containsString("MY_CONFIG"));
        assertThat(chunks.getFirst().text(), containsString("${server.port}"));
        assertThat(chunks.get(1).type(), is(MarkdownPreprocessor.Chunk.Type.CODE));
        assertThat(chunks.get(1).sectionPath(), is("WebServer > Configuration"));
        assertThat(chunks.get(1).text(), containsString("Map<String, List<Integer>>"));
        assertThat(chunks.get(2).sectionPath(), is("WebServer > Routing"));
    }

    @Test
    void preservesConfigurationHtmlTablesAndWordBreaks() {
        var chunks = preprocessor.extractChunks("""
                # io.<wbr>helidon.<wbr>WebServer

                <table>
                <thead><tr><th>Key</th><th>Type</th><th>Description</th></tr></thead>
                <tbody><tr><td><code>content-<wbr>encoding</code></td>
                <td><code>Map&lt;<wbr>String, Value&gt;</code></td>
                <td>Use ${MY_CONFIG} &amp; keep underscores_here.</td></tr></tbody>
                </table>
                """);
        assertThat(chunks.size(), is(1));
        assertThat(chunks.getFirst().type(), is(MarkdownPreprocessor.Chunk.Type.TABLE));
        assertThat(chunks.getFirst().sectionPath(), is("io.helidon.WebServer"));
        assertThat(chunks.getFirst().text(), containsString("Key | Type | Description"));
        assertThat(chunks.getFirst().text(), containsString("content-encoding | Map<String, Value>"));
        assertThat(chunks.getFirst().text(), containsString("${MY_CONFIG} & keep underscores_here."));
    }

    @Test
    void preservesMarkdownTablesAndLists() {
        var chunks = preprocessor.extractChunks("""
                # Options

                | Key | Default |
                | --- | --- |
                | `my_config` | `${PORT}` |

                - Configure the server.
                - Start the listener.
                """);
        assertThat(chunks.size(), is(2));
        assertThat(chunks.getFirst().type(), is(MarkdownPreprocessor.Chunk.Type.TABLE));
        assertThat(chunks.getFirst().text(), containsString("my_config | ${PORT}"));
        assertThat(chunks.get(1).type(), is(MarkdownPreprocessor.Chunk.Type.LIST));
        assertThat(chunks.get(1).text(), containsString("Configure the server."));
        assertThat(chunks.get(1).text(), containsString("Start the listener."));
    }

    @Test
    void preservesDiagramDescriptionsWithoutImageFiles() {
        var chunks = preprocessor.extractChunks("""
                # Agentic Workflows

                ![Agentic workflow with sequence and conditional](images/workflow.svg)

                <img src="images/agents.svg" alt="LangChain4j agents in Helidon">
                """);
        assertThat(chunks.size(), is(2));
        assertThat(chunks.getFirst().text(), is("Agentic workflow with sequence and conditional"));
        assertThat(chunks.get(1).text(), is("LangChain4j agents in Helidon"));
    }

    @Test
    void stripsSiteMetadataButKeepsBodyAndCodeComments() {
        var chunks = preprocessor.extractChunks("""
                ---
                title: Site-only title
                ---
                <!--@frontmatter
                description: hidden metadata
                -->
                # Setup
                <!--@mdc ::code-callout -->
                Keep this explanation.
                <!--@include includes/other.md -->
                <!--@mdc :: -->

                ```xml
                <!-- application configuration -->
                <version>${helidon.version}</version>
                ```
                """);
        String text = chunks.stream().map(MarkdownPreprocessor.Chunk::text).collect(Collectors.joining());
        assertThat(text, not(containsString("Site-only")));
        assertThat(text, not(containsString("hidden metadata")));
        assertThat(text, not(containsString("@mdc")));
        assertThat(text, not(containsString("@include")));
        assertThat(text, containsString("Keep this explanation."));
        assertThat(text, containsString("<!-- application configuration -->"));
        assertThat(text, containsString("<version>${helidon.version}</version>"));
    }

    @Test
    void boundsOversizedBlocksWithoutLosingCode() {
        String code = "Map<String, String> my_config = settings;\n".repeat(80) + "x".repeat(2200) + "\n";
        var chunks = preprocessor.extractChunks("# Example\n\n```java\n" + code + "```\n");
        assertThat(chunks.stream().allMatch(chunk -> chunk.text().length() <= 1000), is(true));
        assertThat(chunks.stream().allMatch(chunk -> chunk.sectionPath().equals("Example")), is(true));
        assertThat(chunks.stream().allMatch(chunk -> chunk.type() == MarkdownPreprocessor.Chunk.Type.CODE), is(true));
        assertThat(chunks.stream().map(MarkdownPreprocessor.Chunk::text).collect(Collectors.joining()), is(code));
    }
}
