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

package io.helidon.hol.agentic.assistant.mcp;

import java.util.List;

import io.helidon.config.Config;
import io.helidon.extensions.mcp.server.Mcp;
import io.helidon.extensions.mcp.server.McpToolContent;
import io.helidon.extensions.mcp.server.McpToolContents;
import io.helidon.json.schema.JsonSchema;
import io.helidon.service.registry.Service;

import static java.lang.System.Logger.Level.INFO;

@Mcp.Path("/cli")
@Mcp.Server("cli-tools-mcp-server")
class CliToolsMcpServer {
    private static final System.Logger LOGGER = System.getLogger(CliToolsMcpServer.class.getName());

    @Service.Inject
    Config config;

    @Mcp.Tool("Returns a version of the latest released Helidon.")
    List<McpToolContent> getLatestHelidonVersion() {
        var version = config.get("app.latest-helidon-version").asString().orElse("4.0.0");
        LOGGER.log(INFO, "Latest released Helidon version: " + version);
        return List.of(McpToolContents.textContent(version));
    }

    @Mcp.Tool("Returns example of Helidon CLI command to create Helidon quickstart example with provided projectName, "
            + "version, package name and Helidon flavor as parameters. "
            + "Resulting CLI command can be used for generating new quickstart project based on Helidon SE. "
            + "Version parameter should be the latest released version unless specified otherwise.")
    List<McpToolContent> getInitHelidonSeProjectWithCliCmd(InitArguments initArguments) {
        LOGGER.log(INFO, "Init with Helidon cli cmd called, version: {0}, project name: {1}",
                   initArguments.version(),
                   initArguments.projectName());
        return List.of(McpToolContents.textContent(
                String.format("""
                                      helidon init --batch \\
                                      --version %s \\
                                      --name %s \\
                                      -Dpackage=%s  \\
                                      -Dflavor=%s \\
                                      -Dapp-type=quickstart
                                      """,
                              initArguments.version(),
                              initArguments.projectName(),
                              initArguments.packageName(),
                              initArguments.flavor()
                )));
    }

    @JsonSchema.Schema
    @JsonSchema.Title("Calendar Event")
    public record InitArguments(@JsonSchema.Required
                                @JsonSchema.Description("Version of the Helidon release to create a quickstart project with.")
                                String version,
                                @JsonSchema.Required
                                @JsonSchema.Description("Desired quickstart project name, also a name of the project folder.")
                                String projectName,
                                @JsonSchema.Required
                                @JsonSchema.Description("Helidon flavor used in the new project, SE or MP are the only options.")
                                String flavor,
                                @JsonSchema.Required
                                @JsonSchema.Description("Java package name of quickstart project created with resulting command.")
                                String packageName) { }
}