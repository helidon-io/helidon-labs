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

import io.helidon.config.Config;
import io.helidon.extensions.mcp.server.McpServerFeature;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;

/**
 * Main class for running an MCP server that manages coffee shop menu.
 */
public class Main {

    private Main() {
    }

    /**
     * Start the application.
     *
     * @param args command line arguments, currently ignored
     */
    public static void main(String[] args) {
        var config = Services.get(Config.class);

        WebServerConfig.builder()
                .config(config.get("server"))
                .routing(Main::setUpRoute)
                .build()
                .start();
    }

    /**
     * Updates HTTP Routing and registers MCP server feature.
     */
    static void setUpRoute(HttpRouting.Builder builder) {
        var config = Services.get(Config.class);
        builder.addFeature(McpServerFeature.builder()
                                   .config(config.get("mcp.server"))
                                   .addTool(new TakeOrderTool())
                                   .addTool(new ListOrdersTool())
                                   .addTool(new MenuTool())
                                   .build());
    }
}
