package com.programming.agent.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import java.util.List;

public class ProblemContextDTO {
    @JSONField(name = "problem_id")
    private int problemId;
    @JSONField(name = "title")
    private String title;
    @JSONField(name = "difficulty")
    private String difficulty;
    @JSONField(name = "knowledge_points")
    private List<String> knowledgePoints;
    @JSONField(name = "problem_content")
    private String problemContent;
    @JSONField(name = "hints")
    private String hints;
    @JSONField(name = "sample_explanation")
    private String sampleExplanation;
    @JSONField(name = "tags")
    private String tags;
    @JSONField(name = "language")
    private String language = "python";
    @JSONField(name = "hint_shown_count")
    private int hintShownCount = 0;
    @JSONField(name = "diagnosed_count")
    private int diagnosedCount = 0;

    // Getters and Setters
    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public List<String> getKnowledgePoints() { return knowledgePoints; }
    public void setKnowledgePoints(List<String> knowledgePoints) { this.knowledgePoints = knowledgePoints; }

    public String getProblemContent() { return problemContent; }
    public void setProblemContent(String problemContent) { this.problemContent = problemContent; }

    public String getHints() { return hints; }
    public void setHints(String hints) { this.hints = hints; }

    public String getSampleExplanation() { return sampleExplanation; }
    public void setSampleExplanation(String sampleExplanation) { this.sampleExplanation = sampleExplanation; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getHintShownCount() { return hintShownCount; }
    public void setHintShownCount(int hintShownCount) { this.hintShownCount = hintShownCount; }

    public int getDiagnosedCount() { return diagnosedCount; }
    public void setDiagnosedCount(int diagnosedCount) { this.diagnosedCount = diagnosedCount; }
}
