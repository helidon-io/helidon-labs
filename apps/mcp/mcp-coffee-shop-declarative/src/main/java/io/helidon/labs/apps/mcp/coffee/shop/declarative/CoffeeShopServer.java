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

package io.helidon.labs.apps.mcp.coffee.shop.declarative;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.helidon.common.LazyValue;
import io.helidon.extensions.mcp.server.Mcp;
import io.helidon.extensions.mcp.server.McpToolContent;
import io.helidon.extensions.mcp.server.McpToolErrorException;
import io.helidon.json.schema.JsonSchema;
import io.helidon.service.registry.Services;
import io.helidon.transaction.Tx;
import io.helidon.transaction.TxException;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import static io.helidon.extensions.mcp.server.McpToolContents.textContent;

/**
 * Helidon coffee shop MCP server.
 */
@Mcp.Path("/mcp-coffee-shop")
@Mcp.Server("mcp-server-coffee-shop")
class CoffeeShopServer {
    private final OrderRepository orderRepository = Services.get(OrderRepository.class);
    private final MenuItemRepository itemRepository = Services.get(MenuItemRepository.class);
    private final LazyValue<List<MenuItem>> menu = LazyValue.create(itemRepository::listOrderById);

    @Mcp.Tool("Provides the coffee shop menu")
    List<McpToolContent> getMenu() {
        String menu = itemRepository.listOrderById()
                .stream()
                .map(item -> Json.createObjectBuilder()
                        .add("name", item.getName())
                        .add("description", item.getDescription())
                        .add("category", item.getCategory())
                        .add("price", item.getPrice())
                        .add("tags", item.getTags())
                        .add("addOns", item.getAddOns())
                        .build())
                .map(JsonObject::toString)
                .collect(Collectors.joining(", "));
        return List.of(textContent(menu));
    }

    @Mcp.Tool("Give the list of orders")
    List<McpToolContent> listOrders() {
        String orders = orderRepository.listOrderById()
                .stream()
                .map(order -> Json.createObjectBuilder()
                        .add("name", order.getName())
                        .add("order-content", order.getContent())
                        .add("price", order.getPrice())
                        .build())
                .map(JsonObject::toString)
                .collect(Collectors.joining(", "));
        return List.of(textContent(orders));
    }

    @Mcp.Tool("Take an order")
    List<McpToolContent> takeOrder(OrderRequest orderRequest) {
        try {
            BigDecimal totalPrice = new BigDecimal(0);

            for (String item : orderRequest.getContent()) {
                BigDecimal price = menu.get().stream()
                        .filter(it -> item.equals(it.getName()))
                        .map(MenuItem::getPrice)
                        .findFirst()
                        .orElseThrow(() -> new McpToolErrorException(textContent("The item is not on the menu")));
                totalPrice = totalPrice.add(price);
            }

            insert(orderRequest.getName(),
                   String.join(", ", orderRequest.getContent()),
                   totalPrice);
        } catch (TxException e) {
            throw new McpToolErrorException(textContent("There was an issue when taking your order"));
        }
        return List.of(textContent("The order was taken successfully"));
    }

    @Tx.Required
    void insert(String name, String content, BigDecimal price) {
        Order order = new Order(name, content, price);
        orderRepository.insert(order);
    }

    @JsonSchema.Schema
    public static class OrderRequest {
        @JsonSchema.Required
        @JsonSchema.Description("Name of the person who make an order")
        private String name;

        @JsonSchema.Required
        @JsonSchema.Array.MinItems(1)
        @JsonSchema.Description("The order list of menu items name")
        private List<String> content;

        public OrderRequest() {
            this.name = "";
            this.content = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getContent() {
            return content;
        }

        public void setContent(List<String> content) {
            this.content = content;
        }
    }
}
