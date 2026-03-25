package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewPlan {
    private Long id;
    private Long userId;
    private Long wrongItemId;
    private LocalDateTime nextReviewTime;
    private Integer reviewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private WrongBookItem wrongBookItem;
}