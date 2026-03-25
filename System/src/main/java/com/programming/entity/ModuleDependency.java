package com.programming.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ModuleDependency {
    private Long id;
    private Long moduleId;
    private Long prerequisiteModuleId;
    private String dependencyType;
    private LocalDateTime createTime;
}
