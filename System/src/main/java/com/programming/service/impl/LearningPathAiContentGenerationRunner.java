package com.programming.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LearningPathAiContentGenerationRunner implements CommandLineRunner {

    @Value("${learning-content.generate:false}")
    private boolean generate;

    @Value("${learning-content.path-id:}")
    private String pathId;

    @Value("${learning-content.level-id:}")
    private String levelId;

    @Autowired
    private LearningPathAiContentGenerationService generationService;

    @Override
    public void run(String... args) {
        if (!generate) {
            return;
        }
        Long parsedPathId = parseLong(pathId);
        Long parsedLevelId = parseLong(levelId);
        LearningPathAiContentGenerationService.GenerationSummary summary =
                generationService.generate(parsedPathId, parsedLevelId);
        log.info("Learning content generation finished: success={}, failure={}",
                summary.getSuccessCount(), summary.getFailureCount());
        for (String failure : summary.getFailures()) {
            log.warn("Learning content generation failure: {}", failure);
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value.trim());
    }
}
