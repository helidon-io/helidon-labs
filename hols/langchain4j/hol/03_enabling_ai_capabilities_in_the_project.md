# 3. Enabling AI Capabilities in the Project

## In This Section, We Will:

- Use **Helidon LangChain4J integration** to enable AI capabilities.
- Add the necessary **Maven dependencies**.
- Configure **Helidon Inject** to work with AI services.
- Add and configure an **OpenAI chat model**.
- Modify our **RESTful service** to interact with the AI model.

---

## 1. Helidon LangChain4J Integration

We will use [LangChain4J (LC4J)](https://github.com/langchain4j/langchain4j/) to implement AI features in our application. Helidon 4.2 provides a dedicated **Helidon-LC4J integration module**, simplifying AI integration in Helidon applications.

### Key Features of Helidon LangChain4J Integration:

- **Helidon Inject Support**
    - Allows **build-time injection** of LangChain4J components into services.

- **Convention over Configuration**
    - Provides **sensible defaults**, reducing manual setup.

- **Declarative Services**
    - Supports LangChain4J's **declarative service** model within Helidon's **declarative programming model**.

- **Helidon MP / CDI Integration**
    - Enables the use of **multiple AI models** and **AI services** in a **CDI environment**.

## 2. Adding Maven Dependencies

To enable Helidon and LangChain4J integration, we need to add several **Maven dependencies** and **annotation processor**.

### Add Dependencies

Add the following dependencies inside the `<dependencies>` section of `pom.xml`:

```xml
<dependency>
    <groupId>io.helidon.integrations.langchain4j</groupId>
    <artifactId>helidon-integrations-langchain4j</artifactId>
</dependency>
<dependency>
    <groupId>io.helidon.integrations.langchain4j.providers</groupId>
    <artifactId>helidon-integrations-langchain4j-providers-open-ai</artifactId>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-embeddings-all-minilm-l6-v2</artifactId>
    <version>${version.lib.langchain4j}</version>
</dependency>
```

### Add Annotation Processor

Since **Helidon Inject** works at build time, we need to add annotation processor `helidon-integrations-langchain4j-codegen` 
to the `maven-compiler-plugin` in `pom.xml`, result should look like the following:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.helidon.codegen</groupId>
                <artifactId>helidon-codegen-apt</artifactId>
                <version>${helidon.version}</version>
            </path>
            <path>
                <groupId>io.helidon.service</groupId>
                <artifactId>helidon-service-codegen</artifactId>
                <version>${helidon.version}</version>
            </path>
            <path>
              <groupId>io.helidon.integrations.langchain4j</groupId>
              <artifactId>helidon-integrations-langchain4j-codegen</artifactId>
              <version>${helidon.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Build and Run the Project
After adding the dependencies, build and run the project:

```sh
mvn clean package
java -jar target/helidon-ai-hol.jar
```

The application should compile and start without errors.

## 3. Adding a Chat Model

Now, let’s add a **chat model** to our project. We will use OpenAI’s **gpt-4o-mini** in **demo mode**.

### Update Application Configuration

Add the following configuration to `src/main/resources/application.yaml`:

```yaml
langchain4j:
  open-ai:
    chat-model:
      enabled: true
      api-key: "demo"
      model-name: "gpt-4o-mini"
```

### Inject the Chat Model into Our Service

After adding the configuration, the chat model will be **available for injection** into Helidon components.

Update `ChatBotService.java` as follows:

```java
import dev.langchain4j.model.chat.ChatLanguageModel;

@Service.Singleton
public class ChatBotService implements HttpService {

    private final ChatLanguageModel chatModel;

    @Service.Inject
    public ChatBotService(ChatLanguageModel chatModel) {
        // Injecting the configured chat model using constructor injection
        this.chatModel = chatModel;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/chat", this::chatWithAssistant);
    }

    private void chatWithAssistant(ServerRequest req, ServerResponse res) {
        // Reading the `question` query parameter from the request. 
        var question = req.query().first("question").orElse("Hello");
        
        // Calling the chat model to get the answer
        var answer = chatModel.generate(question);

        // Return the answer
        res.send(answer);
    }
}
```

## 4. Testing the AI Chat Assistant

To test the assistant, open the following URL in your browser:

```
http://localhost:8080/chat?question=Hello
```

Or, use `curl`:

```sh
curl -X GET "http://localhost:8080/chat?question=Hello"
```

You should receive an AI-generated response.

---

### Next Step → [Building AI Service](04_building_ai_service.md)
