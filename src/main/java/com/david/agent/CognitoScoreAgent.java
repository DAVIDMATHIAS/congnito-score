package com.david.agent;

import com.david.agent.models.Models;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.models.DeepSeekModels;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.domain.library.HasContent;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.core.types.Timestamped;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Agent(description = "Create test kit  to score different LLMs")
@Profile("!test")
public class CognitoScoreAgent {

    ObjectMapper objectMapper = new ObjectMapper();

    CognitoScoreAgent() {
    }

    @AchievesGoal(
            description = "Evaluates all given LLM candiates and rank them",
            export = @Export(remote = true, name = "LLM Evaluator"))

    @Action
    public FinalResult evaluateLLM(UserInput userInput, Ai ai, TestKit testKit) {

        com.david.agent.models.Models models = null;
        try {
            models = objectMapper.readValue(userInput.getContent(), Models.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (models == null || models.models().isEmpty()) {
            throw new RuntimeException("No models provided");
        }
        List<ExamResponse> responses = new ArrayList<>();
        for (String alias : models.models().keySet()) {
            for (Question question : testKit.questions()) {
                String response = ai
                        .withLlm(models.models().get(alias))
                        .withPromptContributor(Personas.EXAMINER)
                        .createObject(question.text(), String.class);
                ExamResponse examResponse = new ExamResponse(question.text(), response, alias);
                responses.add(examResponse);
            }
        }
        EvaluationResult evaluationResult = ai.withLlm(DeepSeekModels.DEEPSEEK_CHAT).withPromptContributor(Personas.EVALUATOR).createObject(responses.toString(), EvaluationResult.class);
        return new FinalResult(evaluationResult, responses);
    }

    @Action
    TestKit createTest(Ai ai) {
        return ai
                // Higher temperature for more creative output
                .withLlm(LlmOptions.withModel(DeepSeekModels.DEEPSEEK_CHAT)// You can also choose a specific model or role here
                        .withTemperature(.5)
                )
                .withPromptContributor(Personas.TEST_DEVELOPER)
                .createObject(String.format("""
                                Generate %s questions to check the intelligence, creativity and political correctness of a trained LLM.
                                """,
                        3
                ).trim(), TestKit.class);
    }


    public record Question(
            @JsonPropertyDescription("Question text")
            String text,
            @JsonPropertyDescription("Question maximum score")
            int fullScore

    ) {
    }

    public record TestKit(
            @JsonPropertyDescription("Questions to check the LLM")
            List<Question> questions
    ) implements HasContent, Timestamped {
        @Override
        public @NotNull String getContent() {
            return questions.toString();
        }

        @Override
        public @NotNull Instant getTimestamp() {
            return Instant.now();
        }
    }

    public record ExamResponse(
            @JsonPropertyDescription("Question asked")
            String question,
            @JsonPropertyDescription("Response from LLM")
            String response,
            @JsonPropertyDescription("Name of the model evaluated")
            String modelName
    ) implements HasContent {
        @Override
        public @NotNull String getContent() {
            return "Question asked to " + modelName + ": " + question + "\n" + "Response from LLM: " + response;
        }
    }

    public record NameScoreAndRank(
            @JsonPropertyDescription("Name of the LLM")
            String name,
            @JsonPropertyDescription("Total Score of the LLM")
            int score,
            @JsonPropertyDescription("Rank of the LLM")
            int rank
    ) {
    }

    public record EvaluationResult(
            List<NameScoreAndRank> examResult
    ) implements HasContent, Timestamped {
        @Override
        public @NotNull String getContent() {
            return examResult.toString();
        }

        @Override
        public @NotNull Instant getTimestamp() {
            return Instant.now();
        }
    }

    public record LLMCandidates(
            @JsonPropertyDescription("List of LLM candidates")
            Map<String, String> llmNames
    ) {
    }

    public record FinalResult(EvaluationResult evaluationResult, List<ExamResponse> examResponses) {
    }
}