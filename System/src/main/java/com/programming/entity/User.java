package com.programming.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class User {
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private Integer role;
    private String language;
    private String abilityProfile;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Role> roles;
    // 个人主页相关字段
    private String bio;
    private String avatarUrl;
    private String githubUrl;
    private String blogUrl;
    private Integer totalSolved;
    private Integer totalSubmissions;
    private Integer studyHours;
    private Integer ranking;
}
