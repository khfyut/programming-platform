package com.programming.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ReferenceSolution {
    private Long id;
    private Long problemId;
    private String language;
    private String solutionCode;
    private String timeComplexity;
    private String spaceComplexity;
    private String explanation;
    private String hints;
    private Date createTime;
    private Date updateTime;
}