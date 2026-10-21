# 4. Building AI Service

## In This Section, We Will:

- Understand the need for an **AI Service** in LLM-powered applications.
- Learn how **LangChain4J AI Services** help combine multiple AI components.
- Implement an **AI Service**.
- Modify our **RESTful service** to use the AI Service instead of calling the chat model directly.

---

## 1. Why Do We Need an AI Service?

LLM-powered applications typically require more than just a **chat model**. To provide **advanced AI functionality**, we also need:

- **Embedding models**
- **Embedding storage**
- **Chat memory** (to remember conversation history)
- **Other AI components**

Instead of managing these separately, we can use **LangChain4J AI Services**, which combine different AI components into a single service and make them work together.

Helidon’s **LangChain4J integration** makes it easy to create AI Services using **Helidon Inject**.

## 2. Creating the AI Service

An **AI Service** is a user-defined interface annotated with `@Ai.Service`. Name the AI service with `@Ai.Service("hol-model")` and select the configured chat model with `@Ai.ChatModel("hol-model")`. Later, we will connect a content retriever to supply relevant menu data.

📌 We will keep all AI-related classes in `io.helidon.hol.lc4j.ai` package. Create this package first if it doesn't exist.

**Create a new file `ChatAiService.java` in `io.helidon.hol.lc4j.ai` package:**

```java
package io.helidon.hol.lc4j.ai;

import io.helidon.extensions.langchain4j.Ai;

@Ai.Service("hol-model")
@Ai.ChatModel("hol-model")
public interface ChatAiService {

    String chat(String question);
}
```

## 3. Updating the RESTful Service

Now, let's modify our existing `ChatBotService.java` to use the **AI Service** instead of interacting with the chat model directly.

### **Before:**

Previously, `ChatModel` was injected into `ChatBotService` and called directly:

```java
var answer = chatModel.chat(question);
```

### **After:**

Now, we will inject and use `ChatAiService` instead:

```java
var answer = chatAiService.chat(question);
```

Here is the updated version of `ChatBotService`:

```java
@Service.Singleton
public class ChatBotService implements HttpService {

    private final ChatAiService chatAiService;

    @Service.Inject
    public ChatBotService(ChatAiService chatAiService) {
        // Injecting ChatAiService using constructor injection
        this.chatAiService = chatAiService;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/chat", this::chatWithAssistant);
    }

    private void chatWithAssistant(ServerRequest req, ServerResponse res) {
        // Reading the `question` query parameter from the request. 
        var question = req.query().first("question").orElse("Hello");

        // Calling the AI service to get the answer
        var answer = chatAiService.chat(question);

        // Return the answer
        res.send(answer);
    }
}
```

## **5. Testing the AI Service**

Recompile and run the application:

```sh
mvn clean package
java -jar target/helidon-ai-hol.jar
```

Now, test it in a browser:

```
http://localhost:8080/chat?question=Hello
```

Or, use `curl`:

```sh
curl -X GET "http://localhost:8080/chat?question=Hello"
```

**Expected Result:** The response should still be an AI-generated answer, just like before.

---

### Next Step → [Adding Retrieval-Augmented Generation (RAG)](05_adding_rag.md)
