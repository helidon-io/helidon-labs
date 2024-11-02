/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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
package io.helidon.labs.apps.chatroom;

import io.helidon.http.BadRequestException;
import io.helidon.http.NotFoundException;
import io.helidon.http.Status;
import io.helidon.http.sse.SseEvent;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

/**
 * Chat service.
 */
class ChatService implements HttpService {

    private static final JsonProvider JSON_PROVIDER = JsonProvider.provider();

    private final ChatRoom room = new ChatRoom();

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.post("/", this::connect)
                .post("/{id}", this::send)
                .get("/{id}", this::events)
                .delete("/{id}", this::disconnect);
    }

    private void connect(ServerRequest req, ServerResponse res) {
        String user = req.content().asOptional(String.class)
                .orElseThrow(() -> new BadRequestException("User required"));
        ChatRoom.Session session = room.newSession(user);
        res.send(session.id());
    }

    private void disconnect(ServerRequest req, ServerResponse res) {
        String id = req.path().pathParameters().get("id");
        session(id).close();
        res.status(Status.NO_CONTENT_204).send();
    }

    private void send(ServerRequest req, ServerResponse res) {
        String id = req.path().pathParameters().get("id");
        String text = req.content().as(String.class);
        session(id).send(text);
        res.send();
    }

    private void events(ServerRequest req, ServerResponse res) {
        String id = req.path().pathParameters().get("id");
        try (SseSink sseSink = res.sink(SseSink.TYPE)) {
            session(id).poll(event -> {
                if (event instanceof ChatRoom.Message message) {
                    JsonObject jsonObject = JSON_PROVIDER.createObjectBuilder()
                            .add("user", message.user())
                            .add("timestamp", message.timestamp().toEpochMilli())
                            .add("text", message.text())
                            .build();
                    sseSink.emit(SseEvent.create(jsonObject));
                }
            });
        }
    }

    private ChatRoom.Session session(String id) {
        return room.session(id).orElseThrow(() -> new NotFoundException(id));
    }
}
