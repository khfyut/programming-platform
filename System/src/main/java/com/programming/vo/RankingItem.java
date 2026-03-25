package com.programming.vo;

import lombok.Data;

@Data
public class RankingItem {
    private Long userId;
    private String username;
    private String avatarUrl;
    private int rank;
    private int score;
    private double passRate;
    private int studyHours;
}