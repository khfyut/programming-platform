package com.programming.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteVO {
    private String targetType;
    private Long targetId;
    private String targetName;
    private LocalDateTime createTime;
}