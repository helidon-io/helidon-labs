# 8. Fine-Tuning the Responses

Now that we have a working AI assistant, we can fine-tune it to provide better responses and a more natural conversational experience.

In this section, we will: 

- Add **chat memory** to maintain conversation context.  
- Customize the assistant’s behavior using a **System Message**.

---

## 1. Adding Chat Memory

**Chat Memory** is a mechanism that stores past interactions in a conversation, allowing an AI model to maintain context, recall previous exchanges, and generate coherent, context-aware responses.

### Why Do We Need Chat Memory?

By default, each user query is treated as an isolated request. This means the AI forgets previous interactions and lacks continuity in the conversation.

In LangChain4J, **chat memory** helps the AI track ongoing discussions, making responses more cohesive and personalized.

### Using `MessageWindowChatMemory`

LangChain4J provides `MessageWindowChatMemory`, a sliding window memory mechanism that keeps only the most recent interactions.

To enable chat memory, annotate the AI service with `@Ai.ChatMemoryWindow(10)`, where `10` is the number of messages to retain.

## 2. Tuning the Prompt (System Message)

### Why Do We Need a System Message?

Currently, users can ask anything, and the assistant tries to answer every question. However, in a business scenario, we may want the assistant to:  

- **Stay on topic** and answer only menu-related questions.  
- **Maintain a specific persona and tone** (e.g., a friendly coffee shop assistant).  
- **Politely refuse off-topic queries**.

### Using a System Message

A **System Message** is a predefined instruction that sets rules for the AI model’s behavior, tone, and personality. It guides responses, ensuring they align with business needs.

We will use the `@SystemMessage` annotation to provide instructions to our AI assistant.

## 3. Updating the AI Service

Modify `ChatAiService.java` to include chat memory and a system message:

```java
@Ai.Service
@Ai.ChatMemoryWindow(10)
public interface ChatAiService {

    @SystemMessage("""
            You are Frank - a friendly coffee shop assistant helping customers with their orders.
            You must not answer any questions not related to the menu or placing orders.
            """)
    String chat(String question);
}
```

**What This Does:**
- 
- **`@Ai.Service`** → Defines the interface as an AI service.
- **`@Ai.ChatMemoryWindow(10)`** → Enables **chat memory** to retain the last **10 messages**.
- **`@SystemMessage`** → Instructs the AI to:  
  - Adopt the persona of **Frank, a friendly coffee shop assistant**.  
  - **Politely refuse** unrelated questions.

## 4. Testing the Fine-Tuned Assistant

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
