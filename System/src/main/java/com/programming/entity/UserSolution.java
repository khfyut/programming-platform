package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserSolution {
    private Long id;
    private Long userId;
    private Long problemId;
    private String title;
    private String content;
    private String code;
    private String language;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private User user;
    private Problem problem;
}