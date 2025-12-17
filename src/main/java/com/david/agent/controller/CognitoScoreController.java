package com.david.agent.controller;

import com.david.agent.CognitoScoreAgent;
import com.david.agent.models.Models;
import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CognitoScoreController {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private AgentPlatform agentPlatform;

    @PostMapping("/api/v1/run")
    public CognitoScoreAgent.FinalResult runAgent(@RequestBody Models models) throws Exception {
        String value = objectMapper.writeValueAsString(models);
        return AgentInvocation.create(agentPlatform, CognitoScoreAgent.FinalResult.class).invoke(new UserInput(value));
    }
}
