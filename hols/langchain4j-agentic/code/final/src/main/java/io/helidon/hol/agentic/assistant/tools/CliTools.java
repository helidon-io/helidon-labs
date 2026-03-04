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

package io.helidon.hol.agentic.assistant.tools;

import io.helidon.common.features.api.HelidonFlavor;
import io.helidon.config.Config;
import io.helidon.service.registry.Service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import static java.lang.System.Logger.Level.INFO;

@Service.Singleton
public class CliTools {
    private static final System.Logger LOGGER = System.getLogger(CliTools.class.getName());
    private final Config config;

    @Service.Inject
    CliTools(Config config) {
        this.config = config;
    }

    @Tool("Returns a version of the latest released Helidon")
    String getLatestHelidonVersion() {
        var version = config.get("app.latest-helidon-version").asString().orElse("4.0.0");
        LOGGER.log(INFO, "Latest released Helidon version: " + version);
        return version;
    }

    @Tool("""
            Returns example of Helidon CLI command to create Helidon quickstart example with provided projectName,
            version, package name and Helidon flavor as parameters.
            Resulting CLI command can be used for generating new quickstart project based on Helidon SE.
            Version parameter should be the latest released version unless specified otherwise.
            """)
    String getInitHelidonSeProjectWithCliCmd(
            @P("Desired quickstart project name, also a name of the project folder") String projectName,
            @P("Version of the Helidon release to create a quickstart project with") String version,
            @P("Desired flavor of Helidon used in the new project, SE or MP are the only options.") HelidonFlavor flavor,
            @P("Java package name of the quickstart project which will be created with resulting command") String packageName) {

        LOGGER.log(INFO, "Init with Helidon cli cmd called, version: {0}, project name: {1}", version, projectName);
        return String.format("""
                                     helidon init --batch \\
                                     --version %s \\
                                     --name %s \\
                                     -Dpackage=%s  \\
                                     -Dflavor=%s \\
                                     -Dapp-type=quickstart
                                     """, version, projectName, packageName, flavor);
    }
}
