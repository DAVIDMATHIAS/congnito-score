package com.david.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.models.DeepSeekModels;
import com.embabel.agent.domain.library.HasContent;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.core.types.Timestamped;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Agent(description = "Create test kit  to score different LLMs")
@Profile("!test")
public class CognitoScoreAgent {

    CognitoScoreAgent() {
    }

    @AchievesGoal(
            description = "Evaluates all given LLM candiates and rank them",
            export = @Export(remote = true, name = "LLM Evaluator"))

    @Action
    public EvaluationResult evaluateLLM(Ai ai, LLMCandidates llmCandidates, TestKit testKit) {
        List<ExamResponse> responses = new ArrayList<>();
        for (String llmName : llmCandidates.llmNames()) {
            for (Question question : testKit.questions()) {
                String response = ai
                        .withLlm(llmName)
                        .withPromptContributor(Personas.EXAMINER)
                        .createObject(question.text(), String.class);
                ExamResponse examResponse = new ExamResponse(question.text(), response, llmName);
                responses.add(examResponse);
            }
        }
        return ai.withLlm(DeepSeekModels.DEEPSEEK_CHAT).withPromptContributor(Personas.EVALUATOR).createObject(responses.toString(), EvaluationResult.class);
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

    @Action
    LLMCandidates candidates() {
        List<String> llmNames = List.of(DeepSeekModels.DEEPSEEK_CHAT);
        return new LLMCandidates(llmNames);
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
            return "Question asked to "+modelName+": "+question+"\n"+"Response from LLM: "+response;
        }
    }

    public record NameAndScore(
            @JsonPropertyDescription("Name of the LLM")
            String name,
            @JsonPropertyDescription("Total Score of the LLM")
            int score
    ){}
    public record EvaluationResult(
            List<NameAndScore> examResult
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
            List<String> llmNames
    ) {
    }
}