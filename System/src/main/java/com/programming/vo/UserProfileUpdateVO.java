package com.programming.vo;

import lombok.Data;

@Data
public class UserProfileUpdateVO {
    private String username;
    private String bio;
    private String avatarUrl;
    private String githubUrl;
    private String blogUrl;
}
