# 2. Preparing Embeddings with Embedding Ingestor

In this HOL we use LangChain4j `InMemoryEmbeddingStore` instances for RAG (one store for Helidon SE and one for Helidon MP).
`InMemoryEmbeddingStore` keeps vectors in RAM for fast retrieval, and it also supports loading persisted state from JSON via `from-file`.

This means we can:

- Prepare embeddings once.
- Save them as JSON files.
- Reuse those files during the HOL, where app projects only load embeddings and do not compute them.

Reference: [LangChain4j In-Memory Embedding Store](https://docs.langchain4j.dev/integrations/embedding-stores/in-memory)

In this section, we will:

- Build and run `embedding-ingestor`.
- Generate `se-embeddings.json` and `mp-embeddings.json` in `hols/langchain4j-agentic/data`.
- Reuse these files in later HOL steps by setting `from-file` in `application.yaml`.

---

## 1. Go to the ingestor project

From repository root (`helidon-labs`):

```sh
cd langchain4j-agentic/code/embedding-ingestor
```

## 2. Build the ingestor

```sh
./mvnw clean package
```

## 3. Run ingestion and serialize embeddings

```sh
java \
--enable-native-access=ALL-UNNAMED \
--add-opens java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens java.base/java.io=ALL-UNNAMED \
-jar ./target/*.jar
```

What this run does:

- Unzips `langchain4j-agentic/data/helidon-docs.zip`.
- Chunks and embeds documents for SE and MP.
- Serializes both in-memory stores to:
  - `langchain4j-agentic/data/se-embeddings.json`
  - `langchain4j-agentic/data/mp-embeddings.json`

## 4. Verify generated files

```sh
ls -lh ../../data/se-embeddings.json ../../data/mp-embeddings.json
```

You should see both files present and non-empty.

## 5. Reuse JSON files later with `from-file`

In a later step these generated files will be referenced in `application.yaml` using `from-file` for both embedding stores so the app loads vectors directly from persisted JSON:

```yaml
langchain4j:
  embedding-stores:
    se-embedding-store:
      provider: lc4j-in-memory
      from-file: ../../data/se-embeddings.json
    mp-embedding-store:
      provider: lc4j-in-memory
      from-file: ../../data/mp-embeddings.json
```

With this setup, HOL projects reuse persisted embeddings and skip embedding computation entirely.
This will happen in a later step. You do not need to do anything at this time.

> [!NOTE]
> Please close all edit windows you may still have open for the embedding-ingestor project at this time.

---

### Next Step -> [Setting Up the Bootstrap Project](02_setting_up_the_bootstrap_project.md)
