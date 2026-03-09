# HOL: Building Agentic AI Applications with Helidon and LangChain4J

In this hands-on lab, you will build an **agentic AI assistant** with **Helidon** and **LangChain4J**.  
Starting from the bootstrap project, you will evolve it into a multi-agent system that classifies user intent, routes requests to specialized experts, applies RAG, and returns summarized conversational context.

## What You'll Learn

- How to build an **agentic workflow** with `@SequenceAgent` and `@ConditionalAgent`.
- How to split responsibilities across agents: classifier, router, expert agents, and summarizer.
- How to configure and use multiple models (cheap vs expensive) for different agent tasks.
- How to use separate **embedding stores** and **content retrievers** for Helidon SE and MP knowledge.
- How to add tool usage with local `@Ai.Tools` and switch to MCP using `@Ai.McpClients`.
- How to transform `code/bootstrap` into the final implementation in `hols/langchain4j-agentic/code/final`.

## Agentic Architecture (Final Project)

Final code location: `hols/langchain4j-agentic/code/final`

![Helidon Agentic Assistant Diagram](./docs/img/agentic_assistant_agents_diagram.svg)

### Helidon Agentic Assistant Overview

The final application uses an **agentic design** built with **LangChain4j Agentic APIs** and integrated into a **Helidon declarative** application model.
Instead of a single chat call, the assistant coordinates multiple agents with distinct responsibilities:

- **`FlavorClassifierAgent`** classifies the request as Helidon **SE** or **MP**.
- **`FlavorRouterAgent`** conditionally routes the request to the right expert agent.
- **`HelidonSeExpert`** and **`HelidonMpExpert`** answer using flavor-specific RAG context.
- **`SummarizerAgent`** maintains concise conversation continuity across requests.

This orchestration is executed by **`HelidonExpertAgent`** as a sequence, giving a predictable, composable multi-agent flow while still running as a standard Helidon service endpoint.

### Local Embeddings and RAG in This HOL

The HOL uses a **local embedding model** (`all-minilm-l6-v2-q`, in-process) and **local in-memory embedding stores** (separate stores for SE and MP), loaded from persisted JSON files.
Embeddings are generated only by the standalone `embedding-ingestor` project in step `2`.
The HOL application projects do not compute embeddings at startup; they load prebuilt embeddings via `from-file`.

### Assistant UI

Open the UI at:

`http://localhost:8080`

## Table of Contents

1. [Setting Up the Environment](./docs/00_setting_up_the_environment.md)
2. [Preparing Embeddings with Embedding Ingestor](./docs/01_preparing_embeddings_with_embedding_ingestor.md)
3. [Setting Up the Bootstrap Project](./docs/02_setting_up_the_bootstrap_project.md)
4. [Configuring the Project for Agentic AI](./docs/03_configuring_the_project_for_agentic_ai.md)
5. [Creating the Orchestrator Agent](./docs/04_creating_the_orchestrator_agent.md)
6. [Adding Specialized Experts and Tools](./docs/05_adding_specialized_experts_and_tools.md)
7. [Wiring RAG and REST Endpoint](./docs/06_wiring_rag_and_rest_endpoint.md)
8. [Building and Running the Final Assistant](./docs/07_building_and_running_the_final_assistant.md)
9. [Bonus: Using MCP Server Instead of Local Tools](./docs/08_bonus_using_mcp_server.md)

Let's get started.

### [Start the Hands-on Lab ->](./docs/00_setting_up_the_environment.md)
