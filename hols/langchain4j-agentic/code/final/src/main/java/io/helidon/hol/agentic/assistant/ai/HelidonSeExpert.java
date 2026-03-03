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
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Agent("helidon-se-expert")
@Ai.ChatModel("expensive-model")
@Ai.ContentRetriever("se-content-retriever")
@Ai.Tools(CliTools.class)
public interface HelidonSeExpert {

    @UserMessage("""
            You are a Helidon SE expert.
            Analyze the following user request about Helidon SE and provide the best possible answer.
            The user request is {{question}}.
            """)
    @Agent(value = "A Helidon SE expert", outputKey = "lastResponse")
    String askExpert(@V("question") String question);
}