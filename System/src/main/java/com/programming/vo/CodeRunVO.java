package com.programming.vo;

import lombok.Data;

@Data
public class CodeRunVO {
    private Long problemId;
    private String code;
    private String language;
    private String input;

}
