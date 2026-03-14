package com.programming.vo;

import lombok.Data;
import java.util.List;

@Data
public class BatchImportProblemsVO {
    private List<ProblemItem> problems;

    @Data
    public static class ProblemItem {
        private String title;
        private String content;
        private String input;
        private String output;
        private Integer difficulty;
        private String language;
        private Integer timeLimit;
        private Integer memoryLimit;
        private String tags;
        private String knowledgePoints;
        private String hints;
        private String sampleExplanation;
    }
}