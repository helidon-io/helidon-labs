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
import io.helidon.extensions.mcp.server.McpToolContents;
import io.helidon.service.registry.Services;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

/**
 * MCP tool that provides the coffee shop menu.
 */
class MenuTool implements McpTool {
    private static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Map.of());
    private final MenuItemRepository repository = Services.get(MenuItemRepository.class);

    @Override
    public String name() {
        return "get-menu";
    }

    @Override
    public String description() {
        return "Provides the coffee shop menu";
    }

    @Override
    public String schema() {
        return "";
    }

    @Override
    public Function<McpRequest, List<McpToolContent>> tool() {
        return request -> {
            String menu = repository.listOrderById()
                    .stream()
                    .map(item -> JSON_FACTORY.createObjectBuilder()
                            .add("name", item.getName())
                            .add("description", item.getDescription())
                            .add("category", item.getCategory())
                            .add("price", item.getPrice())
                            .add("tags", item.getTags())
                            .add("addOns", item.getAddOns())
                            .build())
                    .map(JsonObject::toString)
                    .collect(Collectors.joining(", "));
            return List.of(McpToolContents.textContent(menu));
        };
    }
}
