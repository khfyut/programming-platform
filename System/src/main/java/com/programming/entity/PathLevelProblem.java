
package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PathLevelProblem {
    private Long id;
    private Long levelId;
    private Long problemId;
    private Integer orderNum;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
