package com.programming.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyActivity {
    private String type;
    private Long targetId;
    private String targetName;
    private String status;
    private LocalDateTime createTime;
}