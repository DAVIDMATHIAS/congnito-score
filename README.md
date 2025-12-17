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
  },
  "evaluatorModel": "deepseek-chat"
}'
```

**Request Body Structure:**
```json
{
  "models": {
    "<alias>": "<model-name>",
    "<alias>": "<model-name>"
  },
  "evaluatorModel": "<evaluator-model-name>"
}
```

- **alias**: Short identifier for the model (e.g., "a", "b", "gpt4", "claude")
- **model-name**: Full model identifier (e.g., "claude-3-7-sonnet-latest", "gpt-4", "deepseek-chat")
- **evaluatorModel**: The LLM used to generate questions and evaluate responses (e.g., "deepseek-chat", "claude-3-7-sonnet-latest")

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
   - Uses the specified evaluator model with a Cognitive Psychologist persona
   - Generates 3 questions targeting intelligence, creativity, and political correctness
   - Runs with temperature 0.5 for balanced creativity

2. **Examination Phase**
   - Each LLM candidate receives all questions **in parallel**
   - Uses ExecutorService with thread pool (up to 10 concurrent threads)
   - Responses are recorded as-is by the Examiner persona
   - Significantly faster for multiple models

3. **Evaluation Phase**
   - Evaluator model scores all responses using an Evaluator persona
   - Assigns scores and ranks to each LLM
   - Returns comprehensive results with all questions and responses

### Personas

The system uses three distinct personas:

- **Test Developer (Cognitive Psychologist)**: PhD in Philosophy; used to work in a recruitment agency. Creates 3 challenging questions to check intelligence, creativity and political correctness.
- **Examiner**: Strict examiner who provides questions one by one and records responses as-is
- **Evaluator**: PhD in Philosophy; used to work in a recruitment agency. Given question-answer pairs from each LLM, provides rank and score for each

## 🛠️ Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Set default LLM for agent operations
embabel.models.default-llm=deepseek-chat

# Configure custom LLM roles (optional)
#embabel.models.llms.best=your-preferred-model
#embabel.models.llms.cheapest=your-budget-model

# Set ranking LLM (optional)
#embabel.agent-platform.ranking.llm=deepseek-chat
```

**Note**: The evaluator model is now specified per request in the API call, not in configuration.

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

### Compare Claude Models

```bash
curl --location 'http://localhost:8080/api/v1/run' \
--header 'Content-Type: application/json' \
--data '{
  "models": {
    "sonnet-3-7": "claude-3-7-sonnet-latest",
    "sonnet-4-5": "claude-sonnet-4-5"
  },
  "evaluatorModel": "deepseek-chat"
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
    "deepseek": "deepseek-chat"
  },
  "evaluatorModel": "deepseek-chat"
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

Control creativity in question generation (default is 0.5):
```java
.withLlm(LlmOptions.withModel(models.evaluatorModel())
    .withTemperature(.7)  // Higher = more creative (0.0-1.0)
)
```

### Customize Thread Pool Size

For optimal performance with many models, adjust the thread pool in `CognitoScoreAgent.evaluateLLM()`:
```java
// Default: Math.min(totalTasks, 10)
ExecutorService executor = Executors.newFixedThreadPool(Math.min(totalTasks, 20));
```

## 📝 License

See [LICENSE](LICENSE) file for details.

## 🤝 Contributing

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for contribution guidelines.

## 📧 Support

For issues or questions, please open an issue on the GitHub repository.

---

**Built with** [Embabel Agent Framework](https://embabel.com) • Spring Boot • Java 17
