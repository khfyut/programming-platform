package com.programming.vo;

import lombok.Data;

@Data
public class MyRankVO {
    private int solvedRank;
    private int passRateRank;
    private int studyHoursRank;
    private int totalSolved;
    private double passRate;
    private int studyHours;
}