# HOL: Building AI-Powered Applications with Helidon and LangChain4J

In this hands-on lab, you will build an AI-powered chat assistant for a coffee shop using **Helidon 27** and **LangChain4J**. The assistant will understand natural language, provide menu recommendations, and guide users through the ordering process.

Requires **JDK 27**. Both the bootstrap and final projects use Helidon `27.0.0`; the final project adds Helidon Extensions LangChain4J `27.0.0`.

Chat examples require an OpenAI API key with access to `gpt-4o-mini`. Set `OPENAI_API_KEY` in the terminal used to start the application; see [Configure OpenAI Access](hol/01_setting_up_the_environment.md#4-configure-openai-access). API usage may incur charges.

## What You'll Learn

- How to use **Helidon** and **LangChain4J** to build an **AI-powered assistant**.
- How to use **Helidon Inject** for dependency injection.
- How to create **embeddings** from structured data and store them in an **embeddings store**.
- How to **query the embeddings store** using natural language.
- What **ingestors** and **content providers** are, and how to implement them.
- How to **fine-tune** your AI assistant for better responses.

## Table of Contents

1. [Setting Up the Environment](./hol/01_setting_up_the_environment.md)
2. [Setting Up the Project](./hol/02_setting_up_the_project.md)
3. [Enabling AI Capabilities in the Project](./hol/03_enabling_ai_capabilities_in_the_project.md)
4. [Building AI Service](./hol/04_building_ai_service.md)
5. [Adding Retrieval-Augmented Generation (RAG)](./hol/05_adding_rag.md)
6. [Preparing Application Data](./hol/06_preparing_application_data.md)
7. [Filling Embedding Store](./hol/07_filling_embedding_store.md)
8. [Fine-Tuning AI Responses](./hol/08_fine_tuning.md)

Let’s get started! 🚀

### [Start the Hands-on Lab →](hol/01_setting_up_the_environment.md)
