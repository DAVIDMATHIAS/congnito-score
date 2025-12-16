
package com.david.agent.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.models.DeepSeekModels;
import com.embabel.agent.domain.library.HasContent;
import com.embabel.agent.prompt.persona.RoleGoalBackstory;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.core.types.Timestamped;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.List;

abstract class Personas {
    static final RoleGoalBackstory TEST_DEVELOPER = RoleGoalBackstory
            .withRole("Cognitive Psychologist")
            .andGoal("Create test kit with  3 questions to check the intelligence, creativity and political correctness of a trained LLM")
            .andBackstory("Has a PhD in Philosophy; used to work in a recruitment agency");

}


@Agent(description = "Create test kit  to score different LLMs")
@Profile("!test")
public class CognitoScoreAgent {

    CognitoScoreAgent() {
    }

    @AchievesGoal(
            description = "Create test kit with  3 questions to check the intelligence, creativity and political correctness of a trained LLM ",
            export = @Export(remote = true, name = "Create Test Kit"))

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

    public record Question(String text) {
    }

    public record TestKit(List<Question> questions)  implements HasContent, Timestamped {
        @Override
        public @NotNull String getContent() {
            return questions.toString();
        }

        @Override
        public @NotNull Instant getTimestamp() {
            return Instant.now();
        }
    }
}