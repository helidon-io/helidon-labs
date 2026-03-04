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

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Service
@Ai.ChatModel("expensive-model")
public interface HelidonExpert {

    @SystemMessage("""
            You are Frank, a helpful Helidon expert.
            
            Only answer questions related to Helidon and its components. If a question is not relevant to Helidon,
            politely decline.
            """)
    @UserMessage("""      
            The user question is:
            {{question}}
            
            Use the following conversation summary to keep context and maintain continuity:
            {{previousSummary}}
            """)
    ExpertMessage chat(@V("question") String question, @V("previousSummary") String previousConversationSummary);

}
