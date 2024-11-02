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

import java.lang.System.Logger.Level;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Chat room.
 */
class ChatRoom {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final List<Event> events = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    /**
     * Get a session by id.
     *
     * @param id session id
     * @return session
     */
    Optional<Session> session(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    /**
     * Create a new session.
     *
     * @param user user
     * @return session
     */
    Session newSession(String user) {
        Session session = sessions.compute(UUID.randomUUID().toString(), (id, current) -> {
            if (current != null) {
                // close existing session
                current.close();
            }
            return new Session(this, id, user);
        });
        // emit all messages
        executor.execute(() -> events.forEach(session::put));
        return session;
    }

    /**
     * Emit an event.
     *
     * @param event event
     */
    void emit(Event event) {
        // save event
        lock.lock();
        try {
            events.add(event);
        } finally {
            lock.unlock();
        }
        // broadcast to all sessions
        sessions.values().forEach(session -> executor.execute(() -> session.put(event)));
    }

    /**
     * Event.
     */
    sealed interface Event permits Message, SessionClose {
    }

    /**
     * Message.
     *
     * @param user      user
     * @param timestamp timestamp
     * @param text      text
     */
    record Message(String user, Instant timestamp, String text) implements Event {

        /**
         * Chat room message.
         *
         * @param user user
         * @param text text
         */
        Message(String user, String text) {
            this(user, Instant.now(), text);
        }
    }

    /**
     * Close event.
     */
    record SessionClose() implements Event {
    }

    /**
     * Session (per user).
     */
    static final class Session {

        private static final System.Logger LOGGER = System.getLogger(Session.class.getName());
        private static final Event EOF = new SessionClose();

        private final AtomicBoolean active = new AtomicBoolean(true);
        private final BlockingQueue<Event> queue = new ArrayBlockingQueue<>(128);
        private final ChatRoom room;
        private final String id;
        private final String user;

        private Session(ChatRoom room, String id, String user) {
            this.room = room;
            this.id = id;
            this.user = user;
        }

        /**
         * Get the id.
         *
         * @return id
         */
        String id() {
            return id;
        }

        /**
         * Send a message.
         *
         * @param text text
         */
        void send(String text) {
            room.emit(new Message(user, text));
        }

        /**
         * Close the session.
         */
        void close() {
            if (active.compareAndSet(true, false)) {
                room.sessions.remove(id);
                if (!queue.offer(EOF)) {
                    LOGGER.log(Level.DEBUG, "Unable to add EOF, session: {0}", id);
                }
            }
        }

        /**
         * Poll messages.
         *
         * @param consumer consumer
         */
        void poll(Consumer<Event> consumer) {
            while (active.get()) {
                try {
                    Event event = queue.take();
                    if (event instanceof SessionClose) {
                        break;
                    }
                    consumer.accept(event);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void put(Event event) {
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
