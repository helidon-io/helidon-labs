# Embedding Ingestor

Standalone ingestor that uses the LangChain4j [in-process embedding model](https://docs.langchain4j.dev/integrations/embedding-models/in-process) together with the LangChain4j [in-memory embedding store](https://docs.langchain4j.dev/integrations/embedding-stores/in-memory) to generate embeddings for Helidon `SE` and `MP`, then serialize each store and their combined `ALL` store to JSON files and bundle them in `embeddings.zip`. These persisted JSON files are meant for the Hands-on Lab quick-start path: instead of re-ingesting docs on each run, you can preload the in-memory stores directly from the saved state using `from-file` configuration.

The ingestor downloads `helidon-markdown-docs.zip` to a temporary file on each run and deletes it afterward. Markdown files from Helidon core and extensions populate the SE store; Helidon MicroProfile Markdown files populate the MP store.

## Run

From `code/embedding-ingestor`:

```sh
./mvnw clean package
```
```sh
java \
--enable-native-access=ALL-UNNAMED \
-jar target/embedding-ingestor.jar
```

Why this JVM flag is used:

- `--enable-native-access=ALL-UNNAMED`:
  Allows unnamed-module libraries to call restricted native APIs. ONNX Runtime loads native code, and this avoids restricted-access warnings/future failures.

If you omit this flag, ingestion may still work, but startup is noisier and future JDKs may enforce stricter access.

The ingestor prints a live console progress bar during document ingestion.

## Expected ONNX Runtime Messages

With the JVM flag shown above, startup output is usually minimal and looks like:

```text
INFO ai.djl.util.Platform - Found matching platform from: .../tokenizers.properties
[####------------------------------] XX.XX% (N/769 files)
```

What they mean:

- `ai.djl.util.Platform ... Found matching platform`: expected startup info from DJL/ONNX. It confirms the native tokenizer runtime was detected correctly.
- Progress bar lines: expected ingestion progress updates.

If you run without the recommended flag, you may also see JDK native-access warnings.

## Config

Configuration file: `src/main/resources/application.yaml`

```yaml
data-dir: ../../data
ingestor:
  docs-zip-uri: https://objectstorage.us-chicago-1.oraclecloud.com/n/axh6g42yikfw/b/lc4j-agents-lab/o/helidon-markdown-docs.zip
  se-embeddings-path: ${data-dir}/se-embeddings.json
  mp-embeddings-path: ${data-dir}/mp-embeddings.json
  all-embeddings-path: ${data-dir}/all-embeddings.json
  embeddings-zip-path: ${data-dir}/embeddings.zip
```
