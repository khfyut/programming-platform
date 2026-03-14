package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private Integer role;
    private String language;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
