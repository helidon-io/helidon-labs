# 1. Setting Up the Environment

Before you start editing code, make sure your machine can build and run the lab projects.

In this section, we will:

- Verify Java and Maven.
- Set required environment variables.
- Confirm folder locations used in this HOL.

---

## 1. Verify Java and Maven

Use Java 21+ and Maven 3.9+:

```sh
java -version
mvn -version
```

## 2. Set OCI API key

This project uses the OpenAI-compatible OCI Generative AI endpoint configured in `application.yaml`.
Set `OCI_API_KEY` before running the app:

```sh
export OCI_API_KEY="<your-oci-api-key>"
```

To persist it:

```sh
echo 'export OCI_API_KEY="<your-oci-api-key>"' >> ~/.bashrc
source ~/.bashrc
```

## 3. Verify project folders

From repository root:

```sh
ls -la hols/langchain4j-agentic/code/bootstrap
ls -la hols/langchain4j-agentic/code/final
ls -la hols/langchain4j-agentic/code/mcp-server
```

---

### Next Step -> [Preparing Embeddings with Embedding Ingestor](00_preparing_embeddings_with_embedding_ingestor.md)
