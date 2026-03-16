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

package io.helidon.hol.agentic.assistant.ai;

import io.helidon.hol.agentic.assistant.tools.CliTools;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Agent("helidon-se-expert")
@Ai.ChatModel("expensive-model")
@Ai.ContentRetriever("se-content-retriever")
@Ai.Tools(CliTools.class)
// Use MCP instead of @Ai.Tools
//@Ai.McpClients("cli-tools-mcp-server")
public interface HelidonSeExpert {

    @SystemMessage("""
            You are a Helidon SE expert.
            
            Use the following conversation summary to keep context and maintain continuity:
            {{previousSummary}}

            Use retrieved Helidon documentation for conceptual guidance.
            When the user asks for the latest Helidon version, or asks for a CLI command without explicitly providing
            a version, call the available tool to get the latest version first.
            Treat every value returned by a tool as authoritative.
            If a tool returns a version string, repeat that exact version string verbatim.
            Never replace, normalize, infer, or update a tool-returned version using your own knowledge or retrieved content.
            If a tool returns a CLI command, copy that command exactly and do not modify the version inside it.
            Analyze the user request about Helidon SE and provide the best possible answer.
            """)
    @UserMessage("""
            The user request is {{question}}.
            """)
    @Agent(value = "A Helidon SE expert", outputKey = "lastResponse")
    String askExpert(@V("question") String question, @V("previousSummary") String previousConversationSummary);
}
