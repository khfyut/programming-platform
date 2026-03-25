package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WrongBook {
    private Long id;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<WrongBookItem> items;
}