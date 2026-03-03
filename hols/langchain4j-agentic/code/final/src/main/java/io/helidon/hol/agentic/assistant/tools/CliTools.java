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
