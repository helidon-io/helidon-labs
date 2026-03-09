# Embedding Ingestor

Standalone ingestor that uses the LangChain4j [in-process embedding model](https://docs.langchain4j.dev/integrations/embedding-models/in-process) together with the LangChain4j [in-memory embedding store](https://docs.langchain4j.dev/integrations/embedding-stores/in-memory) to generate embeddings for Helidon `SE` and `MP`, then serialize each store to JSON files. These persisted JSON files are meant for the Hands-on Lab quick-start path: instead of re-ingesting docs on each run, you can preload the in-memory stores directly from the saved state using `from-file` configuration.

## Run

From `code/embedding-ingestor`:

```sh
mvnd clean package
```
```sh
java \
--enable-native-access=ALL-UNNAMED \
--sun-misc-unsafe-memory-access=allow \
--add-opens java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens java.base/java.io=ALL-UNNAMED \
-jar ./target/*.jar
```

Why these JVM flags are used:

- `--enable-native-access=ALL-UNNAMED`:
  Allows unnamed-module libraries to call restricted native APIs. ONNX Runtime loads native code, and this avoids restricted-access warnings/future failures.
- `--sun-misc-unsafe-memory-access=allow`:
  Keeps compatibility with libraries that still touch deprecated `sun.misc.Unsafe` memory methods (for example JRuby used by AsciidoctorJ). This reduces warning noise on newer JDKs.
- `--add-opens java.base/sun.nio.ch=ALL-UNNAMED`:
  Opens internal NIO channel internals used by JRuby/native subprocess helpers (transitively used by Asciidoctor). Without it, you typically see `FilenoUtil` open-access warnings.
- `--add-opens java.base/java.io=ALL-UNNAMED`:
  Opens `java.io` internals for the same JRuby/native interop path and suppresses related module-access warnings.

If you omit these flags, ingestion may still work, but startup is noisier and future JDKs may enforce stricter access.

The ingestor prints a live console progress bar during document ingestion.

## Expected ONNX Runtime Messages

With the JVM flags shown above, startup output is usually minimal and looks like:

```text
INFO ai.djl.util.Platform - Found matching platform from: .../tokenizers.properties
[####------------------------------] XX.XX% (N/163 files)
```

What they mean:

- `ai.djl.util.Platform ... Found matching platform`: expected startup info from DJL/ONNX. It confirms the native tokenizer runtime was detected correctly.
- Progress bar lines: expected ingestion progress updates.

If you run without the recommended flags, you may also see extra JRuby/JDK warnings (for example `sun.misc.Unsafe` and `FilenoUtil` access warnings).

## Config

Configuration file: `src/main/resources/application.yaml`

```yaml
app:
  docs-zip-path: ../../data/helidon-docs.zip
  se-embeddings-path: ../../data/se-embeddings.json
  mp-embeddings-path: ../../data/mp-embeddings.json
```
