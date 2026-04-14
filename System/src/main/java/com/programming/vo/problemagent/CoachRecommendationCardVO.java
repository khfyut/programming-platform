package com.programming.vo.problemagent;

import lombok.Data;

@Data
public class CoachRecommendationCardVO {
    private String type;
    private String targetId;
    private String title;
    private String description;
    private String reason;
    private String route;
    private String actionType;
}
