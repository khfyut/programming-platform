package com.programming.service.impl;

import com.programming.entity.LearningPath;
import com.programming.entity.LearningResource;
import com.programming.entity.PathChapter;
import com.programming.entity.PathLevel;
import com.programming.entity.PathLevelProblem;
import com.programming.entity.Problem;
import com.programming.mapper.LearningPathMapper;
import com.programming.mapper.LearningResourceMapper;
import com.programming.mapper.PathLevelProblemMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.service.ai.AiLlmClient;
import com.programming.service.ai.AiLlmConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningPathAiContentGenerationServiceTest {

    @Mock
    private LearningPathMapper learningPathMapper;
    @Mock
    private LearningResourceMapper learningResourceMapper;
    @Mock
    private PathLevelProblemMapper pathLevelProblemMapper;
    @Mock
    private ProblemMapper problemMapper;
    @Mock
    private AiLlmClient aiLlmClient;

    @InjectMocks
    private LearningPathAiContentGenerationService service;

    @Test
    @SuppressWarnings("unchecked")
    void generateForLevelOverwritesGeneratedResourcesAndBindsOnlyValidatedRecommendedProblems() throws Exception {
        configureAi();
        LearningPath path = path(1L, "Spring Boot基础", "java");
        PathChapter chapter = chapter(10L, 1L);
        PathLevel level = level(20L, 10L, "统一响应体", "Spring Boot,JSON");
        Problem valid = problem(101L, "返回JSON响应", "java", "Spring Boot,JSON");
        Problem wrongLanguage = problem(102L, "Python响应", "python", "Spring Boot,JSON");
        Problem unrelated = problem(103L, "数组求和", "java", "数组");
        Problem fallback = problem(104L, "Controller基础", "java", "Spring Boot,Controller");

        when(learningPathMapper.selectPathById(1L)).thenReturn(path);
        when(learningPathMapper.selectChaptersByPathId(1L)).thenReturn(List.of(chapter));
        when(learningPathMapper.selectLevelsByChapterId(10L)).thenReturn(List.of(level));
        when(problemMapper.selectAll()).thenReturn(List.of(valid, wrongLanguage, unrelated, fallback));
        when(aiLlmClient.chat(any(AiLlmConfig.class), any(), anyDouble())).thenReturn("""
                {
                  "guide": {
                    "title": "统一响应体学习指引",
                    "summary": "掌握 code/message/data 的响应结构。",
                    "learning_objectives": ["理解响应字段", "能返回JSON"],
                    "key_points": ["Spring Boot", "JSON"],
                    "study_steps": ["阅读题意", "定义DTO", "返回对象"],
                    "completion_criteria": ["能通过测试"]
                  },
                  "practice": {
                    "title": "统一响应体练习建议",
                    "summary": "围绕当前关卡练习。",
                    "recommended_problem_ids": [102, 101, 999],
                    "practice_order": ["先做基础响应题"],
                    "pitfalls": ["不要返回普通字符串"],
                    "pass_criteria": ["输出合法JSON"]
                  }
                }
                """);

        LearningPathAiContentGenerationService.GenerationSummary summary = service.generate(1L, null);

        assertEquals(1, summary.getSuccessCount());
        assertEquals(0, summary.getFailureCount());
        verify(learningResourceMapper).deleteByLevelIdAndTypeAndName(20L, "tutorial", "Spring Boot基础 学习指引");
        verify(learningResourceMapper).deleteByLevelIdAndTypeAndName(20L, "example", "Spring Boot基础 练习建议");
        verify(learningResourceMapper).deleteByLevelIdAndTypeAndName(20L, "tutorial", level.getName() + " 学习指引");
        verify(learningResourceMapper).deleteByLevelIdAndTypeAndName(20L, "example", level.getName() + " 练习建议");

        ArgumentCaptor<LearningResource> resourceCaptor = ArgumentCaptor.forClass(LearningResource.class);
        verify(learningResourceMapper, org.mockito.Mockito.times(2)).insertResource(resourceCaptor.capture());
        List<LearningResource> resources = resourceCaptor.getAllValues();
        assertEquals(List.of("tutorial", "example"), resources.stream().map(LearningResource::getType).toList());
        assertTrue(resources.get(0).getUrl().contains("## 学习目标"));
        assertTrue(resources.get(1).getUrl().contains("## 推荐题目"));

        verify(pathLevelProblemMapper).deleteByLevelId(20L);
        ArgumentCaptor<List<PathLevelProblem>> bindingCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(pathLevelProblemMapper).batchInsert(bindingCaptor.capture());
        List<PathLevelProblem> bindings = bindingCaptor.getValue();
        assertEquals(List.of(101L, 104L), bindings.stream().map(PathLevelProblem::getProblemId).toList());
        verify(learningPathMapper).updateLevel(any(PathLevel.class));
    }

    @Test
    void generateSkipsLevelWhenAiFailsAndDoesNotOverwriteExistingData() throws Exception {
        configureAi();
        LearningPath path = path(1L, "Spring Boot基础", "java");
        PathChapter chapter = chapter(10L, 1L);
        PathLevel level = level(20L, 10L, "统一响应体", "Spring Boot,JSON");

        when(learningPathMapper.selectPathById(1L)).thenReturn(path);
        when(learningPathMapper.selectChaptersByPathId(1L)).thenReturn(List.of(chapter));
        when(learningPathMapper.selectLevelsByChapterId(10L)).thenReturn(List.of(level));
        when(problemMapper.selectAll()).thenReturn(List.of(problem(101L, "返回JSON响应", "java", "Spring Boot,JSON")));
        when(aiLlmClient.chat(any(AiLlmConfig.class), any(), anyDouble())).thenThrow(new RuntimeException("timeout"));

        LearningPathAiContentGenerationService.GenerationSummary summary = service.generate(1L, null);

        assertEquals(0, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertTrue(summary.getFailures().get(0).contains("levelId=20"));
        verify(learningResourceMapper, never()).deleteByLevelIdAndTypeAndName(any(), any(), any());
        verify(pathLevelProblemMapper, never()).deleteByLevelId(any());
        verify(pathLevelProblemMapper, never()).batchInsert(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateFallsBackToSameLanguagePublishedProblemsWhenKnowledgeDoesNotMatch() throws Exception {
        configureAi();
        LearningPath path = path(1L, "Java Path", "java");
        PathChapter chapter = chapter(10L, 1L);
        PathLevel level = level(20L, 10L, "Message Queue", "queue");
        Problem publishedSameLanguage = problem(201L, "Array Sum", "java", "array");
        publishedSameLanguage.setStatus("PUBLISHED");
        Problem draftSameLanguage = problem(202L, "Draft Queue", "java", "queue");
        draftSameLanguage.setStatus("DRAFT");
        Problem publishedOtherLanguage = problem(203L, "Python Queue", "python", "queue");
        publishedOtherLanguage.setStatus("PUBLISHED");

        when(learningPathMapper.selectPathById(1L)).thenReturn(path);
        when(learningPathMapper.selectChaptersByPathId(1L)).thenReturn(List.of(chapter));
        when(learningPathMapper.selectLevelsByChapterId(10L)).thenReturn(List.of(level));
        when(problemMapper.selectAll()).thenReturn(List.of(publishedSameLanguage, draftSameLanguage, publishedOtherLanguage));
        when(aiLlmClient.chat(any(AiLlmConfig.class), any(), anyDouble())).thenReturn("""
                {
                  "guide": {
                    "title": "Guide",
                    "summary": "Summary",
                    "learning_objectives": ["Learn"],
                    "key_points": ["queue"],
                    "study_steps": ["Read"],
                    "completion_criteria": ["Done"]
                  },
                  "practice": {
                    "title": "Practice",
                    "summary": "Practice summary",
                    "recommended_problem_ids": [202, 203, 201],
                    "practice_order": ["Do the published Java problem"],
                    "pitfalls": ["Avoid drafts"],
                    "pass_criteria": ["Pass one problem"]
                  }
                }
                """);

        LearningPathAiContentGenerationService.GenerationSummary summary = service.generate(1L, null);

        assertEquals(1, summary.getSuccessCount());
        assertEquals(0, summary.getFailureCount());
        ArgumentCaptor<List<PathLevelProblem>> bindingCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(pathLevelProblemMapper).batchInsert(bindingCaptor.capture());
        assertEquals(List.of(201L), bindingCaptor.getValue().stream().map(PathLevelProblem::getProblemId).toList());
    }

    private void configureAi() {
        ReflectionTestUtils.setField(service, "aiProvider", "openai-compatible");
        ReflectionTestUtils.setField(service, "aiModel", "deepseek-v4-flash");
        ReflectionTestUtils.setField(service, "ollamaUrl", "http://127.0.0.1:11434");
        ReflectionTestUtils.setField(service, "apiUrl", "https://api.deepseek.com/v1");
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
    }

    private LearningPath path(Long id, String name, String language) {
        LearningPath path = new LearningPath();
        path.setId(id);
        path.setName(name);
        path.setLanguage(language);
        path.setDirection("backend");
        path.setDescription("后端学习路径");
        return path;
    }

    private PathChapter chapter(Long id, Long pathId) {
        PathChapter chapter = new PathChapter();
        chapter.setId(id);
        chapter.setPathId(pathId);
        chapter.setName("基础章节");
        return chapter;
    }

    private PathLevel level(Long id, Long chapterId, String name, String knowledgePoints) {
        PathLevel level = new PathLevel();
        level.setId(id);
        level.setChapterId(chapterId);
        level.setName(name);
        level.setKnowledgePoints(knowledgePoints);
        return level;
    }

    private Problem problem(Long id, String title, String language, String knowledgePoints) {
        Problem problem = new Problem();
        problem.setId(id);
        problem.setTitle(title);
        problem.setLanguage(language);
        problem.setKnowledgePoints(knowledgePoints);
        problem.setDifficulty(0);
        problem.setContent("题目内容");
        return problem;
    }
}
