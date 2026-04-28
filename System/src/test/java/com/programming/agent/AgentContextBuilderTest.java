package com.programming.agent;

import com.programming.agent.dto.AgentContextDTO;
import com.programming.agent.dto.AgentCoachChatRequestDTO;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.UserProblemInteraction;
import com.programming.service.problemagent.ProblemCoachContext;
import com.programming.vo.problemagent.ProblemAgentChatRequestVO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentContextBuilderTest {

    private final AgentContextBuilder builder = new AgentContextBuilder();

    @Test
    void mapsProblemPageIdeaRequestBeforeSubmission() {
        ProblemCoachContext coachContext = coachContext("manual", null);
        ProblemAgentChatRequestVO request = request("这道题的解题思路是什么", false);
        UserProblemInteraction interaction = interaction();

        AgentContextDTO context = builder.buildForProblemCoach(coachContext, request, interaction, 0);

        assertEquals("PROBLEM_PAGE_CHAT", context.getTriggerSource());
        assertEquals("ASK_PROBLEM_SOLVING_IDEA", context.getUserIntent());
        assertEquals(0, context.getConsecutiveFailures());
        assertEquals("FIRST_TRY", context.getLearningStage());
        assertEquals("给定整数数组 nums 和目标值 target。", context.getProblem().getProblemContent());
        assertEquals("先查补数，再记录当前数字。", context.getProblem().getHints());
        assertEquals("nums[0] + nums[1] = target。", context.getProblem().getSampleExplanation());
        assertEquals("数组,哈希表", context.getProblem().getTags());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "这道题的思路是什么",
            "这题应该怎么做",
            "这题应该怎么想",
            "先给方向，不要给完整代码"
    })
    void mapsIdeaPhrasesToProblemSolvingIntent(String message) {
        AgentContextDTO context = builder.buildForProblemCoach(
                coachContext("manual", null),
                request(message, false),
                interaction(),
                0
        );

        assertEquals("PROBLEM_PAGE_CHAT", context.getTriggerSource());
        assertEquals("ASK_PROBLEM_SOLVING_IDEA", context.getUserIntent());
    }

    @Test
    void mapsRunFailureToRunResultAndErrorIntent() {
        ProblemCoachContext coachContext = coachContext("run_failed", null);
        coachContext.setLatestErrorMessage("运行时错误");

        AgentContextDTO context = builder.buildForProblemCoach(coachContext, request("", false), interaction(), 1);

        assertEquals("RUN_RESULT", context.getTriggerSource());
        assertEquals("ASK_FOR_ERROR_ANALYSIS", context.getUserIntent());
        assertEquals(1, context.getConsecutiveFailures());
    }

    @Test
    void mapsSubmissionFailureWithSubmitContext() {
        Submit submit = new Submit();
        submit.setId(9L);
        submit.setUserId(3L);
        submit.setProblemId(1L);
        submit.setResult(1);
        submit.setCode("class Solution {}");
        submit.setLanguage("java");

        AgentContextDTO context = builder.buildForSubmission(problem(), submit, interaction(), 2);

        assertEquals("SUBMISSION_RESULT", context.getTriggerSource());
        assertEquals("ASK_FOR_ERROR_ANALYSIS", context.getUserIntent());
        assertEquals(9, context.getSubmission().getSubmitId());
        assertEquals("WA", context.getSubmission().getErrorMessage());
        assertEquals(2, context.getConsecutiveFailures());
    }

    @Test
    void buildsGlobalGuideContextWithLowPrivilegePolicy() {
        AgentCoachChatRequestDTO request = new AgentCoachChatRequestDTO();
        request.setMessage("这个系统怎么用");
        request.setCurrentRoute("/problems");
        request.setPageType("PROBLEM_LIST");

        AgentContextDTO context = builder.buildForGlobalGuideChat(request);

        assertEquals("global_guide", context.getScene());
        assertEquals("GLOBAL_GUIDE_CHAT", context.getTriggerSource());
        assertEquals("GLOBAL_GUIDE", context.getPolicyProfile());
        assertEquals("global_guide_chat", context.getActionHint());
        assertEquals("GENERAL_CHAT", context.getUserIntent());
        assertTrue(context.getCandidateActions().contains("EXPLAIN"));
        assertTrue(context.getCandidateActions().contains("RECOMMEND"));
        assertTrue(context.getCandidateActions().contains("CLARIFY_INTENT"));
    }

    private ProblemCoachContext coachContext(String triggerType, Submit submit) {
        ProblemCoachContext context = new ProblemCoachContext();
        context.setUserId(3L);
        context.setCurrentProblem(problem());
        context.setCurrentCode("class Solution {}");
        context.setLanguage("java");
        context.setTriggerType(triggerType);
        context.setLatestSubmit(submit);
        return context;
    }

    private Problem problem() {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setTitle("两数之和");
        problem.setDifficulty(1);
        problem.setKnowledgePoints("数组,哈希表");
        problem.setContent("给定整数数组 nums 和目标值 target。");
        problem.setHints("先查补数，再记录当前数字。");
        problem.setSampleExplanation("nums[0] + nums[1] = target。");
        problem.setTags("数组,哈希表");
        problem.setLanguage("java");
        return problem;
    }

    private ProblemAgentChatRequestVO request(String message, boolean fullSolution) {
        ProblemAgentChatRequestVO request = new ProblemAgentChatRequestVO();
        request.setProblemId(1L);
        request.setMessage(message);
        request.setRequestFullSolution(fullSolution);
        request.setCode("class Solution {}");
        request.setLanguage("java");
        return request;
    }

    private UserProblemInteraction interaction() {
        UserProblemInteraction interaction = new UserProblemInteraction();
        interaction.setHintCount(0);
        interaction.setDiagnoseCount(0);
        interaction.setExplainCount(0);
        interaction.setRecommendCount(0);
        interaction.setReflectCount(0);
        interaction.setHasViewedAnswer(false);
        interaction.setLearningStage("FIRST_TRY");
        return interaction;
    }
}
