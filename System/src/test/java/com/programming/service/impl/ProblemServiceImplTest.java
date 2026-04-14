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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
}
