package com.programming.service;

import com.programming.vo.QualityReport;

public interface ContentQualityService {
    /**
     * 验证题目质量
     */
    QualityReport validateProblem(Long problemId);
    
    /**
     * 批量验证题目质量
     */
    void validateProblemsBatch();
    
    /**
     * 生成质量报告
     */
    void generateQualityReport();
    
    /**
     * 优化内容质量
     */
    void optimizeContentQuality();
}
