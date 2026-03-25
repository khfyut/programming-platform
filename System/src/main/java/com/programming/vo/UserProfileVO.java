package com.programming.vo;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String bio;
    private String githubUrl;
    private String blogUrl;
    private StudyStats stats;
    private Integer ranking;
    private boolean isAdmin;
}