package com.programming.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserBehavior {
    private Long id;
    private Long userId;
    private String behaviorType;
    private String targetType;
    private Long targetId;
    private String details;
    private Date createTime;
}