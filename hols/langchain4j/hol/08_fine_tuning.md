# 8. Fine-Tuning the Responses

Now that we have a working AI assistant, we can fine-tune it to provide better responses and a more natural conversational experience.

In this section, we will: 

- Customize the assistant’s behavior using a **System Message**.

---

This example handles each request independently and does not configure conversation memory.

## 1. Tuning the Prompt (System Message)

### Why Do We Need a System Message?

Currently, users can ask anything, and the assistant tries to answer every question. However, in a business scenario, we may want the assistant to:  

- **Stay on topic** and answer only menu-related questions.  
- **Maintain a specific persona and tone** (e.g., a friendly coffee shop assistant).  
- **Politely refuse off-topic queries**.

### Using a System Message

A **System Message** is a predefined instruction that sets rules for the AI model’s behavior, tone, and personality. It guides responses, ensuring they align with business needs.

We will use the `@SystemMessage` annotation to provide instructions to our AI assistant.

## 2. Updating the AI Service

Modify `ChatAiService.java` to include a system message:

```java
package io.helidon.hol.lc4j.ai;

import io.helidon.extensions.langchain4j.Ai;

import dev.langchain4j.service.SystemMessage;

@Ai.Service("hol-model")
@Ai.ChatModel("hol-model")
@Ai.ContentRetriever("hol-content-retriever")
public interface ChatAiService {

    @SystemMessage("""
            You are Frank - a friendly coffee shop assistant helping customers with their orders.
            You must not answer any questions not related to the menu or placing orders.
            """)
    String chat(String question);
}
```

**What This Does:**
- **`@Ai.Service("hol-model")`** → Names the AI service.
- **`@Ai.ChatModel("hol-model")`** → Selects the named chat model.
- **`@Ai.ContentRetriever("hol-content-retriever")`** → Supplies relevant menu data.
- **`@SystemMessage`** → Instructs the AI to:  
  - Adopt the persona of **Frank, a friendly coffee shop assistant**.  
  - **Politely refuse** unrelated questions.

## 3. Testing the Fine-Tuned Assistant

**Recompile and run the application:**

```sh
mvn clean package
java -jar target/helidon-ai-hol.jar
```

**Try asking an unrelated question:**

```
http://localhost:8080/chat?question="What is the weather today?"
```

**Expected response:**

*"I'm here to help with the menu and taking orders! Would you like to know about our coffee options or maybe our pastries?"*

**Now ask a relevant question:**

```
http://localhost:8080/chat?question="What coffee do you have?"
```

**Expected response:**

*"We offer a variety of coffee drinks, including Latte, Espresso, Cappuccino, and more! Would you like to customize your drink?"*

---

### Congratulations! You Have Completed the Hands-On Lab!  
