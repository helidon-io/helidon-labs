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

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class BaseTest {
    private static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Map.of());

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        Main.setUpRoute(builder);
    }

    abstract McpClient client();

    @Test
    @Order(1)
    void testListTools() {
        List<ToolSpecification> tools = client().listTools();
        assertThat(tools.size(), is(3));

        ToolSpecification takingOrder = tools.getFirst();
        assertThat(takingOrder.name(), is("take-order"));
        assertThat(takingOrder.description(), is("Take an order"));
        assertThat(takingOrder.parameters().properties().isEmpty(), is(false));

        ToolSpecification orderManager = tools.get(1);
        assertThat(orderManager.name(), is("list-order"));
        assertThat(orderManager.description(), is("Give the list of orders"));
        assertThat(orderManager.parameters().properties().isEmpty(), is(true));

        ToolSpecification menuManager = tools.get(2);
        assertThat(menuManager.name(), is("get-menu"));
        assertThat(menuManager.description(), is("Provides the coffee shop menu"));
        assertThat(menuManager.parameters().properties().isEmpty(), is(true));
    }

    @Test
    @Order(2)
    void testGetMenu() {
        var result = client().executeTool(ToolExecutionRequest.builder()
                                                  .name("get-menu")
                                                  .build());
        assertThat(result.isError(), is(false));
        assertThat(result.resultText(), containsString("Latte"));
    }

    @Test
    @Order(3)
    void testListEmptyOrders() {
        var result = client().executeTool(ToolExecutionRequest.builder()
                                                  .name("list-order")
                                                  .build());
        assertThat(result.isError(), is(false));
        assertThat(result.resultText(), containsString("Espresso"));
    }

    @Test
    @Order(4)
    void testTakeOrder() {
        var result = client().executeTool(ToolExecutionRequest.builder()
                                                  .name("take-order")
                                                  .arguments(JSON_FACTORY.createObjectBuilder()
                                                                     .add("name", "Frank")
                                                                     .add("content", JSON_FACTORY.createArrayBuilder()
                                                                             .add("Hot Chocolate"))
                                                                     .build()
                                                                     .toString())
                                                  .build());
        assertThat(result.isError(), is(false));
        assertThat(result.resultText(), is("The order was taken successfully"));
    }

    @Test
    @Order(5)
    void testListOrders() {
        var result = client().executeTool(ToolExecutionRequest.builder()
                                                  .name("list-order")
                                                  .build());
        assertThat(result.isError(), is(false));
        assertThat(result.resultText(), containsString("Frank"));
    }
}
