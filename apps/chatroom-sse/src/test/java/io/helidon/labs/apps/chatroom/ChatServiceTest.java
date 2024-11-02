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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.sse.SseSource;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import static io.helidon.http.HeaderValues.ACCEPT_EVENT_STREAM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
class ChatServiceTest {

    private final Http1Client client;

    ChatServiceTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRules rules) {
        rules.register(new ChatService());
    }

    @Test
    void testEvents() throws Exception {
        // connect
        String id = client.post().submit("Joe", String.class).entity();

        // events
        CountDownLatch latch = new CountDownLatch(3);
        CompletableFuture<List<JsonObject>> future = CompletableFuture.supplyAsync(() -> {
            try (var response = client.get(id).header(ACCEPT_EVENT_STREAM).request()) {
                List<JsonObject> events = new ArrayList<>();
                response.source(SseSource.TYPE, event -> {
                    events.add(event.data(JsonObject.class));
                    latch.countDown();
                });
                return events;
            }
        });

        // send 3 messages
        for (int i=0; i < 3; i++) {
            try (var response = client.post(id).submit("Message" + i)) {
                assertThat(response.status(), is(Status.OK_200));
            }
        }

        // wait for 3 messages
        if (!latch.await(5, TimeUnit.SECONDS)) {
           throw new TimeoutException();
        }

        // disconnect
        try (var response = client.delete(id).request()) {
            assertThat(response.status(), is(Status.NO_CONTENT_204));
        }

        List<JsonObject> events = future.get(5, TimeUnit.SECONDS);
        assertThat(events.size(), is(3));
        for (int i=0; i < 3; i++) {
            assertThat(events.get(i), is(notNullValue()));
            assertThat(events.get(i).getString("text"), is("Message" + i));
        }
    }
}
