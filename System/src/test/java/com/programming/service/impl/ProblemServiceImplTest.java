package com.programming.service.impl;

import com.programming.entity.Problem;
import com.programming.entity.ProblemSupportedLanguage;
import com.programming.mapper.FavoriteMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.ProblemSupportedLanguageMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.service.runtime.RuntimeCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProblemServiceImplTest {
    private final ProblemMapper problemMapper = mock(ProblemMapper.class);
    private final ProblemSupportedLanguageMapper problemSupportedLanguageMapper =
            mock(ProblemSupportedLanguageMapper.class);
    private final RuntimeCatalogService runtimeCatalogService = new RuntimeCatalogService();

    private ProblemServiceImpl problemService;

    @BeforeEach
    void setUp() {
        problemService = new ProblemServiceImpl();
        ReflectionTestUtils.setField(problemService, "problemMapper", problemMapper);
        ReflectionTestUtils.setField(problemService, "submitMapper", mock(SubmitMapper.class));
        ReflectionTestUtils.setField(problemService, "favoriteMapper", mock(FavoriteMapper.class));
        ReflectionTestUtils.setField(problemService, "problemSupportedLanguageMapper", problemSupportedLanguageMapper);
        ReflectionTestUtils.setField(problemService, "runtimeCatalogService", runtimeCatalogService);
    }

    @Test
    void filtersHistoricalCategoryLanguageAndFallsBackToDefaultJudgeLanguage() {
        ProblemSupportedLanguage historicalCategory = new ProblemSupportedLanguage();
        historicalCategory.setProblemId(1L);
        historicalCategory.setLanguageCode("algorithm");
        historicalCategory.setIsDefault(1);
        historicalCategory.setStatus("ACTIVE");

        Problem problem = new Problem();
        problem.setId(1L);
        problem.setLanguage("algorithm");

        when(problemSupportedLanguageMapper.findByProblemId(1L))
                .thenReturn(List.of(historicalCategory));
        when(problemMapper.findById(1L)).thenReturn(problem);

        List<ProblemSupportedLanguage> languages = problemService.getSupportedLanguages(1L);

        assertEquals(1, languages.size());
        assertEquals("java", languages.get(0).getLanguageCode());
        assertEquals("Main.java", languages.get(0).getStarterFilename());
    }

    @Test
    void allowsDefaultJudgeLanguageWhenOnlyHistoricalCategoryLanguageExists() {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setLanguage("database");

        when(problemSupportedLanguageMapper.findActiveLanguageCodesByProblemId(1L))
                .thenReturn(List.of("database"));
        when(problemMapper.findById(1L)).thenReturn(problem);

        assertTrue(problemService.isLanguageEnabledForProblem(1L, "java"));
    }

    @Test
    void passesCategoryFilterToProblemListQueries() {
        when(problemMapper.findByPage(0, 20, null, null, null, 3L)).thenReturn(List.of());
        when(problemMapper.count(null, null, null, 3L)).thenReturn(0);

        Map<String, Object> result = problemService.getProblemList(1, 20, null, null, null, 3L, null);

        assertEquals(0, result.get("total"));
        verify(problemMapper).findByPage(0, 20, null, null, null, 3L);
        verify(problemMapper).count(null, null, null, 3L);
    }

    @Test
    void returnsProblemCategoryStatsFromMapper() {
        List<Map<String, Object>> stats = List.of(
                Map.of("id", 3L, "name", "数据结构", "problemCount", 12)
        );
        when(problemMapper.getCategoryStats()).thenReturn(stats);

        assertEquals(stats, problemService.getCategories());
    }
}
