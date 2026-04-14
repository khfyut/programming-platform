package com.programming.vo.problemagent;

import lombok.Data;

@Data
public class CoachActionVO {
    private String type;
    private String label;
    private String prompt;
    private String route;
    private String targetId;
}
