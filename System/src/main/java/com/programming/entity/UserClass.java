package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserClass {
    private Long id;
    private Long userId;
    private Long classId;
    private String role;
    private LocalDateTime joinTime;
    private User user;
    private ClassInfo classInfo;
}