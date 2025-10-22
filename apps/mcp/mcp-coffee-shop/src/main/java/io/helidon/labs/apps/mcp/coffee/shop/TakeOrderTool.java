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

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import io.helidon.common.LazyValue;
import io.helidon.common.mapper.OptionalValue;
import io.helidon.extensions.mcp.server.McpParameters;
import io.helidon.extensions.mcp.server.McpRequest;
import io.helidon.extensions.mcp.server.McpTool;
import io.helidon.extensions.mcp.server.McpToolContent;
import io.helidon.extensions.mcp.server.McpToolErrorException;
import io.helidon.service.registry.Services;
import io.helidon.transaction.Tx;
import io.helidon.transaction.TxException;

import static io.helidon.extensions.mcp.server.McpToolContents.textContent;

/**
 * Take an {@link io.helidon.labs.apps.mcp.coffee.shop.Order} from customer.
 */
class TakeOrderTool implements McpTool {
    private final OrderRepository orderRepository = Services.get(OrderRepository.class);
    private final MenuItemRepository itemRepository = Services.get(MenuItemRepository.class);
    private final LazyValue<List<MenuItem>> menu = LazyValue.create(itemRepository::listOrderById);

    @Override
    public String name() {
        return "take-order";
    }

    @Override
    public String description() {
        return "Take an order";
    }

    @Override
    public String schema() {
        return """
                {
                    "type": "object",
                    "description": "Description of a new order",
                    "properties": {
                        "name": {
                            "type": "string",
                            "description": "Name of the person who make an order"
                        },
                        "content": {
                            "description": "The order list of menu items name",
                            "type": "array",
                            "items": {
                                "type": "string"
                            },
                            "minItems": 1
                        }
                    },
                    "required": [ "name", "content" ]
                }
                """;
    }

    @Override
    public Function<McpRequest, List<McpToolContent>> tool() {
        return request -> {
            try {
                BigDecimal totalPrice = new BigDecimal(0);
                String name = request.parameters()
                        .get("name")
                        .asString()
                        .orElseThrow(() -> new McpToolErrorException(textContent("Name is missing")));
                List<String> names = request.parameters()
                        .get("content")
                        .asList()
                        .get()
                        .stream()
                        .map(McpParameters::asString)
                        .map(OptionalValue::get)
                        .toList();

                for (String itemName : names) {
                    BigDecimal price = menu.get()
                            .stream()
                            .filter(it -> itemName.equals(it.getName()))
                            .map(MenuItem::getPrice)
                            .findFirst()
                            .orElseThrow(() -> new McpToolErrorException(textContent("The item is not on the menu")));
                    totalPrice = totalPrice.add(price);
                }
                final BigDecimal finalTotalPrice = totalPrice;

                Tx.transaction(() -> {
                    Order order = new Order(name, String.join(", ", names), finalTotalPrice);
                    return orderRepository.insert(order);
                });
            } catch (TxException e) {
                throw new McpToolErrorException(textContent("There was an issue when taking your order."));
            }
            return List.of(textContent("The order was taken successfully"));
        };
    }
}
