package com.programming.service;

import com.programming.vo.QualityReport;

import java.util.List;
import java.util.Map;

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

    Map<String, Object> getSummary();

    List<Map<String, Object>> getProblemQualityRows();

    List<Map<String, Object>> getPathBindingQualityRows();

    List<Map<String, Object>> getTagQualityRows();
}
