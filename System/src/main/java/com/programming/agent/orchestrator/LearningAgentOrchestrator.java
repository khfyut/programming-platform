package com.programming.agent.orchestrator;

import com.programming.agent.AgentPolicyConfig;
import com.programming.agent.AgentPolicyProfile;
import com.programming.agent.dto.AgentContextDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LearningAgentOrchestrator {
    private final LearningToolRegistry toolRegistry;
    private final PromptLayerBuilder promptLayerBuilder;
    private final LearningMemoryService memoryService;

    public LearningAgentOrchestrator(LearningToolRegistry toolRegistry,
                                     PromptLayerBuilder promptLayerBuilder,
                                     LearningMemoryService memoryService) {
        this.toolRegistry = toolRegistry;
        this.promptLayerBuilder = promptLayerBuilder;
        this.memoryService = memoryService;
    }

    public LearningAgentRun prepare(LearningAgentEvent event, AgentContextDTO context) {
        AgentContextDTO effectiveContext = context == null ? new AgentContextDTO() : context;
        AgentPolicyProfile profile = event != null && event.getPolicyProfile() != null
                ? event.getPolicyProfile()
                : AgentPolicyProfile.PROBLEM_COACH;
        List<Map<String, Object>> toolResults = toolRegistry == null ? List.of() : toolRegistry.executeFor(event);
        Map<String, Object> learningSummary = memoryService == null ? Map.of() : memoryService.summarize(event, effectiveContext);
        effectiveContext.setPolicyProfile(profile.name());
        effectiveContext.setToolResults(toolResults);
        effectiveContext.setLearningSummary(learningSummary);
        effectiveContext.setCandidateActions(candidateActions(profile));
        if (promptLayerBuilder != null) {
            effectiveContext.setPromptLayers(promptLayerBuilder.build(event, effectiveContext, profile, toolResults, learningSummary));
        }
        return new LearningAgentRun(event, effectiveContext, profile);
    }

    private List<String> candidateActions(AgentPolicyProfile profile) {
        return new ArrayList<>(AgentPolicyConfig.forProfile(profile).getAllowedActions());
    }
}
