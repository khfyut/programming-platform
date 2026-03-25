package com.programming.vo;

import lombok.Data;

@Data
public class StudyStats {
    private int totalSolved;
    private int totalSubmissions;
    private double passRate;
    private int studyHours;
    private int postCount;
    private int commentCount;
    // 周趋势
    private int solvedTrend;
    private int submittedTrend;
    private double accuracyTrend;
    private int streakTrend;
    private int studyHoursTrend;
    private int rankingTrend;
}