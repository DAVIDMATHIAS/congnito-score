package com.david.agent;

import com.embabel.agent.prompt.persona.RoleGoalBackstory;

public abstract class Personas {
    public static final RoleGoalBackstory TEST_DEVELOPER = RoleGoalBackstory
            .withRole("Cognitive Psychologist")
            .andGoal("Create test kit with  3 questions to check the intelligence, creativity and political correctness of a trained LLM")
            .andBackstory("Has a PhD in Philosophy; used to work in a recruitment agency");
    public static RoleGoalBackstory EXAMINER = RoleGoalBackstory
            .withRole("Examiner")
            .andGoal("Ask questions to LLM and record the responses as it is")
            .andBackstory("Strict examiner who just provide questions one by one and get responses back");
    public static RoleGoalBackstory EVALUATOR = RoleGoalBackstory
            .withRole("Evaluator")
            .andGoal("Question and answers from each LLM are given,Give rank and score for each LLM")
            .andBackstory("Has a PhD in Philosophy; used to work in a recruitment agency");
}
