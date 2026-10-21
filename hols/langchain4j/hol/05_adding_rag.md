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

## 2. Configuring the Embedding Model and Store

The in-process provider creates the quantized `AllMiniLmL6V2` embedding model, while the in-memory provider creates the embedding store. Both are registered as named services from configuration, so no Java factory classes are needed.

Replace the `langchain4j` section in `application.yaml` with the following, keeping the existing `server` configuration:

```yaml
langchain4j:
  providers:
    open-ai:
      api-key: "demo"

    lc4j-in-process:
      type: all_minilm_l6_v2_q

    lc4j-content-retriever:
      embedding-model: hol-embedding-model
      max-results: 20
      min-score: 0.6

  models:
    hol-embedding-model:
      provider: lc4j-in-process

    hol-model:
      provider: open-ai
      model-name: "gpt-4o-mini"

  embedding-stores:
    hol-embedding-store:
      provider: lc4j-in-memory

  content-retrievers:
    hol-content-retriever:
      provider: lc4j-content-retriever
      embedding-store: hol-embedding-store
```

The content retriever uses `hol-embedding-model` to embed each question and searches `hol-embedding-store`. It returns up to 20 matches with a minimum similarity score of 0.6.

## 3. Connecting RAG to Our AI Service

Select the named content retriever on `ChatAiService`:

```java
@Ai.Service("hol-model")
@Ai.ChatModel("hol-model")
@Ai.ContentRetriever("hol-content-retriever")
public interface ChatAiService {
    String chat(String question);
}
```

The service now uses the configured chat model and supplies retrieved menu data as context when answering a question.

## 4. Testing RAG Integration

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
- Initially, **no data** is in the embedding store, so results may be empty.
- In the **next step**, we will ingest menu data into the embedding store!

---

### Next Step → [Preparing Application Data](06_preparing_application_data.md)
