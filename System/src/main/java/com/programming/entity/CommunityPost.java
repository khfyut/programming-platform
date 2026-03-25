package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommunityPost {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type; // discussion, note, share
    private Long pathId;
    private Long chapterId;
    private Long levelId;
    private Integer likes;
    private Integer comments;
    private Integer views;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private User user; // 关联用户信息
}
