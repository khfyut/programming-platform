package com.programming.agent.orchestrator;

import com.programming.agent.AgentPolicyProfile;
import com.programming.agent.dto.AgentContextDTO;

public class LearningAgentRun {
    private final LearningAgentEvent event;
    private final AgentContextDTO context;
    private final AgentPolicyProfile policyProfile;

    public LearningAgentRun(LearningAgentEvent event, AgentContextDTO context, AgentPolicyProfile policyProfile) {
        this.event = event;
        this.context = context;
        this.policyProfile = policyProfile;
    }

    public LearningAgentEvent getEvent() {
        return event;
    }

    public AgentContextDTO getContext() {
        return context;
    }

    public AgentPolicyProfile getPolicyProfile() {
        return policyProfile;
    }
}
