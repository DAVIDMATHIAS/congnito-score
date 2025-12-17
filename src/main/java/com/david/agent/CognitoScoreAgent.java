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
import java.util.concurrent.*;

@Agent(description = "Create test kit  to score different LLMs")
@Profile("!test")
public class CognitoScoreAgent {

    ObjectMapper objectMapper = new ObjectMapper();

    CognitoScoreAgent() {
    }

    @AchievesGoal(
            description = "Evaluates all given LLM candidates and rank them",
            export = @Export(remote = true, name = "LLM Evaluator", startingInputTypes = Models.class))

    @Action
    public FinalResult evaluateLLM(UserInput userInput, Ai ai, TestKit testKit, Models models) {

        if (models == null || models.models().isEmpty()) {
            throw new RuntimeException("No models provided");
        }
        
        // Create an executor service with a thread pool
        int totalTasks = models.models().size() * testKit.questions().size();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(totalTasks, 10));
        List<Future<ExamResponse>> futures = new ArrayList<>();
        
        // Submit all tasks to the executor
        for (String modelAlias : models.models().keySet()) {
            for (Question question : testKit.questions()) {
                Future<ExamResponse> future = executor.submit(() -> {
                    String response = ai
                            .withLlm(models.models().get(modelAlias))
                            .withPromptContributor(Personas.EXAMINER)
                            .createObject(question.text(), String.class);
                    return new ExamResponse(question.text(), response, modelAlias);
                });
                futures.add(future);
            }
        }
        
        // Collect all results
        List<ExamResponse> responses = new ArrayList<>();
        for (Future<ExamResponse> future : futures) {
            try {
                responses.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Error executing test", e);
            }
        }
        
        // Shutdown the executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
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