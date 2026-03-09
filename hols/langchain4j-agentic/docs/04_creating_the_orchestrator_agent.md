# 4. Creating the Orchestrator Agent

In this section, we will:

- Replace the single expert AI service with an agentic orchestrator.
- Add flavor classification and routing.
- Add a summarizer agent to maintain the conversation context.

---

## 1. Remove old AI service

Delete:

```text
src/main/java/io/helidon/hol/agentic/assistant/ai/HelidonExpert.java
```

## 2. Create `HelidonExpertAgent.java`

`src/main/java/io/helidon/hol/agentic/assistant/ai/HelidonExpertAgent.java`

```java
package io.helidon.hol.agentic.assistant.ai;

import io.helidon.hol.agentic.assistant.dto.ExpertMessage;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;

@Ai.Agent("helidon-expert")
public interface HelidonExpertAgent {

    @SequenceAgent(outputKey = "jsonResponse", subAgents = {
            FlavorClassifierAgent.class,
            FlavorRouterAgent.class,
            SummarizerAgent.class
    })
    @SystemMessage("""
            You are Frank, a helpful Helidon expert.
            
            Only answer questions related to Helidon and its components. If a question is not relevant to Helidon,
            politely decline.
            
            Use the following conversation summary to keep context and maintain continuity:
            {{previousSummary}}
            """)
    ExpertMessage chat(@V("question") String question, @V("previousSummary") String previousConversationSummary);

    @Output
    static ExpertMessage createResponse(@V("lastResponse") String lastResponse, @V("nextSummary") String nextSummary) {
        return new ExpertMessage(lastResponse, nextSummary);
    }
}
```

## 3. Create `FlavorClassifierAgent.java`

`src/main/java/io/helidon/hol/agentic/assistant/ai/FlavorClassifierAgent.java`

```java
package io.helidon.hol.agentic.assistant.ai;

import io.helidon.common.features.api.HelidonFlavor;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Agent("flavor-classifier")
@Ai.ChatModel("cheap-model")
public interface FlavorClassifierAgent {

    @UserMessage("""
            Analyze the following user request about Helidon framework and categorize it as 'mp' - MicroProfile flavor 
            or 'se' - Standard Edition flavor of Helidon.
            
            In case the request doesn't belong to any of those categories categorize it as 'se'.
            Reply with only one of those words and nothing else.
            The user request is: '{{question}}'.
            """)
    @Agent(value = "Categorize a user request", outputKey = "flavor")
    HelidonFlavor classify(@V("question") String question);
}
```

## 4. Create `FlavorRouterAgent.java`

`src/main/java/io/helidon/hol/agentic/assistant/ai/FlavorRouterAgent.java`

```java
package io.helidon.hol.agentic.assistant.ai;

import io.helidon.common.features.api.HelidonFlavor;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.declarative.ActivationCondition;
import dev.langchain4j.agentic.declarative.ConditionalAgent;
import dev.langchain4j.service.V;

import static java.lang.System.Logger.Level.INFO;

@Ai.Agent("flavor-router")
public interface FlavorRouterAgent {

    System.Logger LOGGER = System.getLogger(FlavorRouterAgent.class.getName());

    @ConditionalAgent(subAgents = {
            HelidonMpExpert.class,
            HelidonSeExpert.class
    })
    String askExpert(@V("question") String question);

    @ActivationCondition(HelidonSeExpert.class)
    static boolean activateSeExpert(@V("flavor") HelidonFlavor flavor) {
        if (flavor == HelidonFlavor.SE) {
            LOGGER.log(INFO, "Activate SE Expert!");
            return true;
        }
        return false;
    }

    @ActivationCondition(HelidonMpExpert.class)
    static boolean activateMpExpert(@V("flavor") HelidonFlavor flavor) {
        if (flavor == HelidonFlavor.MP) {
            LOGGER.log(INFO, "Activate MP Expert!");
            return true;
        }
        return false;
    }
}
```

## 5. Create `SummarizerAgent.java`

`src/main/java/io/helidon/hol/agentic/assistant/ai/SummarizerAgent.java`

```java
package io.helidon.hol.agentic.assistant.ai;

import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@Ai.Agent("summarizer")
@Ai.ChatModel("cheap-model")
public interface SummarizerAgent {

    @SystemMessage("""
        You are a conversation summarizer for an AI assistant. Your job is to keep a concise summary of the
        ongoing conversation to preserve context.
        Given the previous summary, the latest user message, and the AI's response, update the summary so it
        reflects the current state of the conversation.
        Keep it short, factual, and focused on what the user is doing or trying to achieve. Avoid rephrasing the
        entire response or repeating long parts verbatim.
        """)
    @UserMessage("""
        Previous Summary:
        {{previousSummary}}
        
        Last User Message:
        {{question}}

        Last AI Response:
        {{lastResponse}}
        """
    )
    @Agent(value = "A Helidon expert summarizer", outputKey = "nextSummary")
    String chat(@V("previousSummary") String previousSummary,
                @V("question") String question,
                @V("lastResponse") String lastResponse);
}
```

---

### Next Step -> [Adding Specialized Experts and Tools](05_adding_specialized_experts_and_tools.md)

