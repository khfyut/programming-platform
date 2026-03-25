package com.programming.vo;

import java.util.List;

public class QualityReport {
    private Long problemId;
    private String problemTitle;
    private int qualityScore;
    private List<String> basicIssues;
    private List<TestCaseValidation> testCaseValidations;
    private DifficultyAssessment difficultyAssessment;
    private List<String> improvementSuggestions;
    private String overallStatus;
    private long validationTime;
    
    // Getters and Setters
    public Long getProblemId() {
        return problemId;
    }
    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }
    public String getProblemTitle() {
        return problemTitle;
    }
    public void setProblemTitle(String problemTitle) {
        this.problemTitle = problemTitle;
    }
    public int getQualityScore() {
        return qualityScore;
    }
    public void setQualityScore(int qualityScore) {
        this.qualityScore = qualityScore;
    }
    public List<String> getBasicIssues() {
        return basicIssues;
    }
    public void setBasicIssues(List<String> basicIssues) {
        this.basicIssues = basicIssues;
    }
    public List<TestCaseValidation> getTestCaseValidations() {
        return testCaseValidations;
    }
    public void setTestCaseValidations(List<TestCaseValidation> testCaseValidations) {
        this.testCaseValidations = testCaseValidations;
    }
    public DifficultyAssessment getDifficultyAssessment() {
        return difficultyAssessment;
    }
    public void setDifficultyAssessment(DifficultyAssessment difficultyAssessment) {
        this.difficultyAssessment = difficultyAssessment;
    }
    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }
    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }
    public String getOverallStatus() {
        return overallStatus;
    }
    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }
    public long getValidationTime() {
        return validationTime;
    }
    public void setValidationTime(long validationTime) {
        this.validationTime = validationTime;
    }
    
    // Nested classes
    public static class TestCaseValidation {
        private Long testCaseId;
        private String status;
        private String errorMessage;
        private long executionTime;
        
        public Long getTestCaseId() {
            return testCaseId;
        }
        public void setTestCaseId(Long testCaseId) {
            this.testCaseId = testCaseId;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public long getExecutionTime() {
            return executionTime;
        }
        public void setExecutionTime(long executionTime) {
            this.executionTime = executionTime;
        }
    }
    
    public static class DifficultyAssessment {
        private int assignedDifficulty;
        private int estimatedDifficulty;
        private String assessment;
        
        public int getAssignedDifficulty() {
            return assignedDifficulty;
        }
        public void setAssignedDifficulty(int assignedDifficulty) {
            this.assignedDifficulty = assignedDifficulty;
        }
        public int getEstimatedDifficulty() {
            return estimatedDifficulty;
        }
        public void setEstimatedDifficulty(int estimatedDifficulty) {
            this.estimatedDifficulty = estimatedDifficulty;
        }
        public String getAssessment() {
            return assessment;
        }
        public void setAssessment(String assessment) {
            this.assessment = assessment;
        }
    }
}
