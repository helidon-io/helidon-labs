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

import io.helidon.hol.agentic.assistant.dto.ExpertMessage;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.V;

@Ai.Agent("helidon-expert")
public interface HelidonExpertAgent {

    @SequenceAgent(outputKey = "jsonResponse", subAgents = {
            FlavorClassifierAgent.class,
            FlavorRouterAgent.class,
            SummarizerAgent.class
    })
    ExpertMessage chat(@V("question") String question, @V("previousSummary") String previousConversationSummary);

    @Output
    static ExpertMessage createResponse(@V("lastResponse") String lastResponse, @V("nextSummary") String nextSummary) {
        return new ExpertMessage(lastResponse, nextSummary);
    }
}
