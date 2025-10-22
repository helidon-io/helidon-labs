/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
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

package io.helidon.labs.apps.mcp.coffee.shop;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.helidon.extensions.mcp.server.McpRequest;
import io.helidon.extensions.mcp.server.McpTool;
import io.helidon.extensions.mcp.server.McpToolContent;
import io.helidon.service.registry.Services;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

import static io.helidon.extensions.mcp.server.McpToolContents.textContent;

/**
 * List {@link io.helidon.labs.apps.mcp.coffee.shop.Order} from the database.
 */
class ListOrdersTool implements McpTool {
    private static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Map.of());
    private final OrderRepository repository = Services.get(OrderRepository.class);

    @Override
    public String name() {
        return "list-order";
    }

    @Override
    public String description() {
        return "Give the list of orders";
    }

    @Override
    public String schema() {
        return "";
    }

    @Override
    public Function<McpRequest, List<McpToolContent>> tool() {
        return request -> {
            String orders = repository.listOrderById()
                    .stream()
                    .map(order -> JSON_FACTORY.createObjectBuilder()
                            .add("name", order.getName())
                            .add("order-content", order.getContent())
                            .add("price", order.getPrice())
                            .build())
                    .map(JsonObject::toString)
                    .collect(Collectors.joining(", "));
            return List.of(textContent(orders));
        };
    }
}
