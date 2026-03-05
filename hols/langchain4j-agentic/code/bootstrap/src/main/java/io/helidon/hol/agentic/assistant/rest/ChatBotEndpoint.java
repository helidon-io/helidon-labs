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

package io.helidon.hol.agentic.assistant.rest;

import io.helidon.hol.agentic.assistant.ai.HelidonExpert;
import io.helidon.hol.agentic.assistant.dto.ExpertMessage;
import io.helidon.hol.agentic.assistant.dto.IngestionProgress;
import io.helidon.hol.agentic.assistant.rag.DocsIngestor;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

import static io.helidon.common.media.type.MediaTypes.APPLICATION_JSON_VALUE;

@RestServer.Endpoint
@Http.Path
@Service.Singleton
class ChatBotEndpoint {

    private final HelidonExpert expert;
    private final DocsIngestor ingestor;

    @Service.Inject
    ChatBotEndpoint(HelidonExpert agent, DocsIngestor ingestor) {
        this.expert = agent;
        this.ingestor = ingestor;
    }

    @Http.GET
    @Http.Path("/progress")
    @Http.Produces(APPLICATION_JSON_VALUE)
    IngestionProgress ingestionProgress() {
        return ingestor.progress();
    }

    @Http.POST
    @Http.Path("/chat")
    @Http.Produces(APPLICATION_JSON_VALUE)
    ExpertMessage chatWithAssistant(@Http.Entity ExpertMessage msg) {
        return expert.chat(msg.message(), msg.summary());
    }
}
