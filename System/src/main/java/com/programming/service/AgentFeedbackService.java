package com.programming.service;

import com.alibaba.fastjson2.JSON;
import com.programming.agent.dto.AgentFeedbackRequestDTO;
import com.programming.entity.AgentFeedbackLog;
import com.programming.entity.LearningEventLog;
import com.programming.mapper.AgentFeedbackLogMapper;
import com.programming.mapper.LearningEventLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AgentFeedbackService {
    private static final Set<String> VALID_FEEDBACK_TYPES = Set.of(
            "accepted",
            "ignored",
            "asked_followup",
            "revealed_solution",
            "applied_draft",
            "solved_after_action"
    );

    private final LearningEventLogMapper learningEventLogMapper;
    private final AgentFeedbackLogMapper agentFeedbackLogMapper;

    public AgentFeedbackService(LearningEventLogMapper learningEventLogMapper,
                                AgentFeedbackLogMapper agentFeedbackLogMapper) {
        this.learningEventLogMapper = learningEventLogMapper;
        this.agentFeedbackLogMapper = agentFeedbackLogMapper;
    }

    @Transactional
    public AgentFeedbackLog recordFeedback(Long userId, AgentFeedbackRequestDTO request) {
        validateRequest(request);
        LearningEventLog event = learningEventLogMapper.findByRequestIdAndUser(request.getRequestId(), userId);
        if (event == null) {
            throw new RuntimeException("Agent event not found");
        }
        validateAgainstEvent(request, event);

        AgentFeedbackLog log = new AgentFeedbackLog();
        log.setRequestId(request.getRequestId());
        log.setUserId(userId);
        log.setProblemId(request.getProblemId());
        log.setSubmitId(request.getSubmitId());
        log.setEntryRefType(request.getEntryRefType());
        log.setEntryRefId(request.getEntryRefId());
        log.setPathId(request.getPathId());
        log.setLevelId(request.getLevelId());
        log.setWrongItemId(request.getWrongItemId());
        log.setActionType(request.getActionType());
        log.setFeedbackType(request.getFeedbackType());
        log.setMetadataJson(toMetadataJson(request.getMetadata()));
        log.setCreateTime(LocalDateTime.now());
        agentFeedbackLogMapper.insert(log);
        return log;
    }

    private void validateRequest(AgentFeedbackRequestDTO request) {
        if (request == null || isBlank(request.getRequestId())) {
            throw new RuntimeException("request_id is required");
        }
        if (isBlank(request.getActionType())) {
            throw new RuntimeException("action_type is required");
        }
        if (isBlank(request.getFeedbackType()) || !VALID_FEEDBACK_TYPES.contains(request.getFeedbackType())) {
            throw new RuntimeException("Invalid feedback_type");
        }
    }

    private void validateAgainstEvent(AgentFeedbackRequestDTO request, LearningEventLog event) {
        if (request.getProblemId() != null && event.getProblemId() != null
                && !Objects.equals(request.getProblemId(), event.getProblemId())) {
            throw new RuntimeException("Feedback problem_id does not match Agent event");
        }
        if (!scopeMatches(request.getEntryRefType(), event.getEntryRefType())) {
            throw new RuntimeException("Feedback entry_ref_type does not match Agent event");
        }
        if (request.getEntryRefId() != null && event.getEntryRefId() != null
                && !Objects.equals(request.getEntryRefId(), event.getEntryRefId())) {
            throw new RuntimeException("Feedback entry_ref_id does not match Agent event");
        }
        if (request.getPathId() != null && event.getPathId() != null
                && !Objects.equals(request.getPathId(), event.getPathId())) {
            throw new RuntimeException("Feedback path_id does not match Agent event");
        }
        if (request.getLevelId() != null && event.getLevelId() != null
                && !Objects.equals(request.getLevelId(), event.getLevelId())) {
            throw new RuntimeException("Feedback level_id does not match Agent event");
        }
        if (request.getWrongItemId() != null && event.getWrongItemId() != null
                && !Objects.equals(request.getWrongItemId(), event.getWrongItemId())) {
            throw new RuntimeException("Feedback wrong_item_id does not match Agent event");
        }
        if (request.getSubmitId() != null && event.getSubmitId() != null
                && !Objects.equals(request.getSubmitId(), event.getSubmitId())) {
            throw new RuntimeException("Feedback submit_id does not match Agent event");
        }
        if (!Objects.equals(request.getActionType(), event.getActionType())) {
            throw new RuntimeException("Feedback action_type does not match Agent event");
        }
    }

    private String toMetadataJson(Map<String, Object> metadata) {
        return metadata == null || metadata.isEmpty() ? "{}" : JSON.toJSONString(metadata);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean scopeMatches(String requestedType, String eventType) {
        if (isBlank(requestedType) || isBlank(eventType)) {
            return true;
        }
        return Objects.equals(requestedType, eventType);
    }
}
