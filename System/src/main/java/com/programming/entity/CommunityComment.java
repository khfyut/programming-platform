package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommunityComment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId; // 父评论ID，用于回复功能
    private Integer likes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private User user; // 关联用户信息
}
