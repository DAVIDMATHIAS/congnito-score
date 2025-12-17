package com.david.agent.controller;

import com.david.agent.CognitoScoreAgent;
import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CognitoScoreController {

    @Autowired
    private AgentPlatform agentPlatform;
    @PostMapping("/api/v1/run")
    public String runAgent(@RequestBody String input){
        CognitoScoreAgent.EvaluationResult output = AgentInvocation.create(agentPlatform, CognitoScoreAgent.EvaluationResult.class).invoke(new UserInput(input));
        return output.getContent();
    }
}
