# Cognito Score 🧠

An intelligent LLM evaluation agent that automatically generates test questions and scores multiple Large Language Models (LLMs) on intelligence, creativity, and political correctness.

## 📋 Overview

Cognito Score uses an AI-powered agent system to:
1. **Generate Test Questions** - Creates customized questions using a Cognitive Psychologist persona
2. **Examine LLMs** - Administers questions to multiple LLM candidates
3. **Evaluate & Rank** - Scores and ranks all LLMs based on their responses

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Gradle
- API keys for LLM providers (Anthropic Claude, DeepSeek, etc.)

### Environment Setup

Set up your API keys as environment variables:

```bash
export ANTHROPIC_API_KEY=your-anthropic-api-key
export DEEPSEEK_API_KEY=your-deepseek-api-key
# Add other provider keys as needed
```

### Build & Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## 🔧 API Usage

### Evaluate LLMs

**Endpoint:** `POST /api/v1/run`

**Request:**
```bash
curl --location 'http://localhost:8080/api/v1/run' \
--header 'Content-Type: application/json' \
--data '{
  "models": {
    "a": "claude-3-7-sonnet-latest",
    "c": "claude-sonnet-4-5"
  }
}'
```

**Request Body Structure:**
```json
{
  "models": {
    "<alias>": "<model-name>",
    "<alias>": "<model-name>"
  }
}
```

- **alias**: Short identifier for the model (e.g., "a", "b", "gpt4", "claude")
- **model-name**: Full model identifier (e.g., "claude-3-7-sonnet-latest", "gpt-4", "deepseek-chat")

**Response Example:**
```json
{
  "evaluationResult": {
    "examResult": [
      {
        "name": "a",
        "score": 85,
        "rank": 1
      },
      {
        "name": "c",
        "score": 78,
        "rank": 2
      }
    ]
  },
  "examResponses": [
    {
      "question": "How would you approach solving climate change?",
      "response": "...",
      "modelName": "a"
    }
  ]
}
```

## 📚 How It Works

### Agent Workflow

1. **Test Creation Phase**
   - Uses DeepSeek Chat with a Cognitive Psychologist persona
   - Generates 3 questions targeting intelligence, creativity, and political correctness

2. **Examination Phase**
   - Each LLM candidate receives all questions
   - Responses are recorded as-is by the Examiner persona

3. **Evaluation Phase**
   - DeepSeek evaluates all responses using an Evaluator persona
   - Assigns scores and ranks to each LLM
   - Returns comprehensive results

### Personas

The system uses three distinct personas:

- **Test Developer (Cognitive Psychologist)**: PhD in Philosophy, creates challenging questions
- **Examiner**: Strict examiner who administers tests objectively
- **Evaluator**: PhD-level expert who scores and ranks LLM performance

## 🛠️ Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Set default LLM for agent operations
embabel.models.default-llm=deepseek-chat

# Configure custom LLM roles
embabel.models.llms.best=your-preferred-model
embabel.models.llms.cheapest=your-budget-model

# Set ranking LLM
embabel.agent-platform.ranking.llm=deepseek-chat
```

### Supported LLM Providers

- **Anthropic Claude** (claude-3-7-sonnet-latest, claude-sonnet-4-5, etc.)
- **DeepSeek** (deepseek-chat, deepseek-reasoner)
- **OpenAI** (optional - uncomment in build.gradle)

## 📁 Project Structure

```
src/main/java/com/david/agent/
├── AppLauncher.java              # Spring Boot application entry point
├── CognitoScoreAgent.java        # Main agent with evaluation logic
├── Personas.java                 # Agent persona definitions
├── controller/
│   └── CognitoScoreController.java  # REST API controller
└── models/
    └── Models.java               # Request model definitions
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test
```

## 📊 Example Use Cases

### Compare GPT-4 vs Claude

```bash
curl --location 'http://localhost:8080/api/v1/run' \
--header 'Content-Type: application/json' \
--data '{
  "models": {
    "gpt4": "gpt-4-turbo",
    "claude": "claude-3-7-sonnet-latest"
  }
}'
```

### Benchmark Multiple Models

```bash
curl --location 'http://localhost:8080/api/v1/run' \
--header 'Content-Type: application/json' \
--data '{
  "models": {
    "claude-opus": "claude-3-opus-latest",
    "claude-sonnet": "claude-3-7-sonnet-latest",
    "deepseek": "deepseek-chat",
    "gpt4": "gpt-4-turbo"
  }
}'
```

## 🔍 Advanced Features

### Custom Question Count

Modify the question count in `CognitoScoreAgent.createTest()`:
```java
.createObject(String.format("""
    Generate %s questions to check the intelligence, creativity 
    and political correctness of a trained LLM.
    """, 5  // Change from 3 to 5 or any number
).trim(), TestKit.class);
```

### Adjust Temperature

Control creativity in question generation:
```java
.withLlm(LlmOptions.withModel(DeepSeekModels.DEEPSEEK_CHAT)
    .withTemperature(.7)  // Higher = more creative
)
```

## 📝 License

See [LICENSE](LICENSE) file for details.

## 🤝 Contributing

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for contribution guidelines.

## 📧 Support

For issues or questions, please open an issue on the GitHub repository.

---

**Built with** [Embabel Agent Framework](https://embabel.com) • Spring Boot • Java 17
