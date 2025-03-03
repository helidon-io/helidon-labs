# 5. Adding Retrieval-Augmented Generation (RAG)

## In This Section, We Will:
- Understand what **RAG** is and why it’s useful.
- Learn about **embedding models**, **embedding storage**, and **content retriever**.
- Implement services for these components using **Helidon Inject**.
- Integrate them into our **AI Service** to enable RAG functionality.

---

## 1. What is RAG?

**Retrieval-Augmented Generation (RAG)** is an advanced AI technique that enhances LLMs (Large Language Models) by allowing them to retrieve **external knowledge** in real-time. Instead of relying solely on pre-trained data, RAG enables AI models to fetch relevant information from external sources, such as knowledge bases, document stores, or **embeddings storage**, leading to more accurate, context-aware responses.

### Key Components of RAG:

1. **Embedding Model** → Converts text into numerical vectors (**embeddings**) for similarity-based retrieval.
2. **Embedding Store** → Stores and retrieves embeddings efficiently.
3. **Content Retriever** → Fetches relevant content from the embedding store based on user queries.

## 2. Creating the Embedding Model and Store

Since our application uses **Helidon Inject**, we need to create **Helidon services** for the following:

- **Embedding Model**
- **Embedding Store**
- **Content Retriever**

We will use **Helidon Producers** to achieve this.

### What is a Producer?

A **Producer** is a **service** that implements `Supplier<T>`, where `T` is the type it produces. This allows us to define custom factories for components like embedding models and stores.

### Step 1: Creating the Embedding Model

We will use the `AllMiniLmL6V2EmbeddingModel`, a pretrained embedding model from LangChain4J.

**Create `EmbeddingModelFactory.java` in `io.helidon.hol.lc4j.ai` package and add the following code:**

```java
package io.helidon.hol.lc4j.ai;

import java.util.function.Supplier;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.helidon.service.registry.Service;

@Service.Singleton
class EmbeddingModelFactory implements Supplier<EmbeddingModel> {
    @Override
    public EmbeddingModel get() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
```

**What This Does:**

- Defines a **service** using `@Service.Singleton`.
- Implements `Supplier<EmbeddingModel>`, making it a factory for the embedding model.
- Returns an instance of `AllMiniLmL6V2EmbeddingModel`.

💡 We implemented the embedding model as a **separate standalone Java class**, but in some cases, it may be more convenient to define producers as **static inner classes** inside related components.

### Step 2: Creating the Embedding Store

Next, we create an **embedding store** that will store and retrieve embeddings efficiently. We will use `InMemoryEmbeddingStore` provided by LangChain4J.

**Create `EmbeddingStoreFactory.java` in `io.helidon.hol.lc4j.ai` package and add the following code:**

```java
package io.helidon.hol.lc4j.ai;

import java.util.function.Supplier;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.helidon.service.registry.Service;

@Service.Singleton
class EmbeddingStoreFactory implements Supplier<EmbeddingStore<TextSegment>> {
    @Override
    public EmbeddingStore<TextSegment> get() {
        return new InMemoryEmbeddingStore<>();
    }
}
```

**What This Does:**

- Defines a **service** using `@Service.Singleton`.
- Implements `Supplier<EmbeddingStore<TextSegment>>`, making it a factory for the embedding store.
- Uses `InMemoryEmbeddingStore<TextSegment>`, a simple in-memory storage solution for embeddings.

### Step 3: Creating the Content Retriever

To connect our embedding store to the **AI Service**, we need a **Content Retriever**. Instead of writing Java code, Helidon allows configuring it directly via YAML.

**Modify `application.yaml` and add the RAG configuration part:**

```yaml
langchain4j:
  ...
  rag:
    embedding-store-content-retriever:
      enabled: true
      max-results: 10
      min-score: 0.6
      embedding-model: "@default"
      embedding-store: "@default"
```

**What This Does:**

- Enables **RAG** by configuring `EmbeddingStoreContentRetriever`.
- Automatically discovers `EmbeddingModel` and `EmbeddingStore` from the service registry using `@default` as a reference.
- Configures key retrieval properties:
    - `max-results: 10` → Returns up to 10 results per query.
    - `min-score: 0.6` → Filters out results with low similarity scores.

## 4. Connecting RAG to Our AI Service

**Do We Need to Modify Our AI Service?**  

No! Helidon LangChain4J automatically discovers the `ContentRetriever` service in the service registry. Since we registered it via configuration, it will be automatically plugged into our **AI Service** without any additional code.

After recompiling, our AI Service will now support RAG, allowing it to query data from the embedding store!

## 5. Testing RAG Integration

### Recompile and Restart the Application:

```sh
mvn clean package
java -jar target/helidon-ai-hol.jar
```

### Test the RAG Querying:

Try asking a **menu-related question** via the chatbot API:

```
http://localhost:8080/chat?question=What drinks do you have?
```

Or use `curl`:

```sh
curl -G -X GET "http://localhost:8080/chat" --data-urlencode "question=What drinks do you have?"
```

**Expected Behavior:**
- 
- Initially, **no data** is in the embedding store, so results may be empty.
- In the **next step**, we will ingest menu data into the embedding store!

---

### Next Step → [Preparing Application Data](06_preparing_application_data.md)
