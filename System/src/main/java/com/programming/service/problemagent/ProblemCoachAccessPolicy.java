package com.programming.service.problemagent;

import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import org.springframework.stereotype.Component;

@Component
public class ProblemCoachAccessPolicy {

    public boolean canRevealFullSolution(boolean hasFailure, ProblemAgentChatRequestVO request) {
        return hasFailure
                && request != null
                && Boolean.TRUE.equals(request.getRequestFullSolution());
    }
}
