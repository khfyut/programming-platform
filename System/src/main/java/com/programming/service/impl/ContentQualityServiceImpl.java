package com.programming.service.impl;

import com.programming.entity.Problem;
import com.programming.entity.TestCase;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.TestCaseMapper;
import com.programming.service.CodeSandboxService;
import com.programming.service.ContentQualityService;
import com.programming.vo.CodeExecutionResult;
import com.programming.vo.QualityReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ContentQualityServiceImpl implements ContentQualityService {
    
    @Autowired
    private ProblemMapper problemMapper;
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Autowired
    private CodeSandboxService codeSandboxService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    @Override
    public QualityReport validateProblem(Long problemId) {
        long startTime = System.currentTimeMillis();
        QualityReport report = new QualityReport();
        
        try {
            Problem problem = problemMapper.findById(problemId);
            if (problem == null) {
                report.setOverallStatus("ERROR");
                report.setBasicIssues(List.of("题目不存在"));
                return report;
            }
            
            report.setProblemId(problemId);
            report.setProblemTitle(problem.getTitle());
            
            // 1. 基础信息检查
            List<String> basicIssues = checkBasicInfo(problem);
            report.setBasicIssues(basicIssues);
            
            // 2. 测试用例验证
            List<QualityReport.TestCaseValidation> testCaseValidations = validateTestCases(problem);
            report.setTestCaseValidations(testCaseValidations);
            
            // 3. 难度评估
            QualityReport.DifficultyAssessment difficultyAssessment = assessDifficulty(problem, testCaseValidations);
            report.setDifficultyAssessment(difficultyAssessment);
            
            // 4. 计算质量分数
            int qualityScore = calculateQualityScore(basicIssues, testCaseValidations, difficultyAssessment);
            report.setQualityScore(qualityScore);
            
            // 5. 生成改进建议
            List<String> improvementSuggestions = generateImprovementSuggestions(basicIssues, testCaseValidations, difficultyAssessment);
            report.setImprovementSuggestions(improvementSuggestions);
            
            // 6. 总体状态
            report.setOverallStatus(qualityScore >= 80 ? "EXCELLENT" : qualityScore >= 60 ? "GOOD" : "NEEDS_IMPROVEMENT");
            
        } catch (Exception e) {
            log.error("验证题目质量失败", e);
            report.setOverallStatus("ERROR");
            report.setBasicIssues(List.of("验证过程发生错误: " + e.getMessage()));
        } finally {
            report.setValidationTime(System.currentTimeMillis() - startTime);
        }
        
        return report;
    }
    
    @Override
    public void validateProblemsBatch() {
        List<Problem> problems = problemMapper.selectAll();
        log.info("开始批量验证题目质量，共 {} 道题目", problems.size());
        
        for (Problem problem : problems) {
            executorService.submit(() -> {
                QualityReport report = validateProblem(problem.getId());
                log.info("题目 {} (ID: {}) 质量评分: {}, 状态: {}", 
                        report.getProblemTitle(), report.getProblemId(), 
                        report.getQualityScore(), report.getOverallStatus());
            });
        }
        
        log.info("批量验证任务已提交");
    }
    
    @Override
    public void generateQualityReport() {
        // 这里可以实现生成详细的质量报告，比如导出Excel或PDF
        log.info("生成质量报告");
        // 实现逻辑...
    }
    
    @Override
    public void optimizeContentQuality() {
        // 这里可以实现自动优化内容质量的逻辑
        log.info("优化内容质量");
        // 实现逻辑...
    }
    
    private List<String> checkBasicInfo(Problem problem) {
        List<String> issues = new ArrayList<>();
        
        if (problem.getTitle() == null || problem.getTitle().trim().isEmpty()) {
            issues.add("题目标题为空");
        }
        
        if (problem.getContent() == null || problem.getContent().trim().isEmpty()) {
            issues.add("题目描述为空");
        } else if (problem.getContent().length() < 100) {
            issues.add("题目描述过于简短");
        }
        
        if (problem.getDifficulty() == null) {
            issues.add("难度等级未设置");
        }
        
        if (problem.getLanguage() == null || problem.getLanguage().trim().isEmpty()) {
            issues.add("编程语言未设置");
        }
        
        if (problem.getTimeLimit() == null || problem.getTimeLimit() <= 0) {
            issues.add("时间限制未设置或无效");
        }
        
        if (problem.getMemoryLimit() == null || problem.getMemoryLimit() <= 0) {
            issues.add("内存限制未设置或无效");
        }
        
        if (problem.getKnowledgePoints() == null || problem.getKnowledgePoints().trim().isEmpty()) {
            issues.add("未关联知识点");
        }
        
        return issues;
    }
    
    private List<QualityReport.TestCaseValidation> validateTestCases(Problem problem) {
        List<QualityReport.TestCaseValidation> validations = new ArrayList<>();
        List<TestCase> testCases = testCaseMapper.findByProblemId(problem.getId());
        
        if (testCases == null || testCases.isEmpty()) {
            QualityReport.TestCaseValidation validation = new QualityReport.TestCaseValidation();
            validation.setStatus("ERROR");
            validation.setErrorMessage("没有测试用例");
            validations.add(validation);
            return validations;
        }
        
        for (TestCase testCase : testCases) {
            QualityReport.TestCaseValidation validation = new QualityReport.TestCaseValidation();
            validation.setTestCaseId(testCase.getId());
            
            try {
                // 这里可以使用代码沙箱执行测试用例
                // 暂时模拟验证
                validation.setStatus("PASS");
                validation.setExecutionTime(100);
            } catch (Exception e) {
                validation.setStatus("FAIL");
                validation.setErrorMessage(e.getMessage());
            }
            
            validations.add(validation);
        }
        
        return validations;
    }
    
    private QualityReport.DifficultyAssessment assessDifficulty(Problem problem, List<QualityReport.TestCaseValidation> testCaseValidations) {
        QualityReport.DifficultyAssessment assessment = new QualityReport.DifficultyAssessment();
        assessment.setAssignedDifficulty(problem.getDifficulty() != null ? problem.getDifficulty() : 0);
        
        // 简单的难度评估逻辑
        int estimatedDifficulty = 0;
        if (problem.getContent() != null && problem.getContent().length() > 500) {
            estimatedDifficulty++;
        }
        if (testCaseValidations.size() > 5) {
            estimatedDifficulty++;
        }
        
        assessment.setEstimatedDifficulty(estimatedDifficulty);
        
        if (Math.abs(assessment.getAssignedDifficulty() - estimatedDifficulty) > 1) {
            assessment.setAssessment("难度等级可能不准确");
        } else {
            assessment.setAssessment("难度等级合理");
        }
        
        return assessment;
    }
    
    private int calculateQualityScore(List<String> basicIssues, List<QualityReport.TestCaseValidation> testCaseValidations, QualityReport.DifficultyAssessment difficultyAssessment) {
        int score = 100;
        
        // 基础信息问题扣分
        score -= basicIssues.size() * 5;
        
        // 测试用例问题扣分
        long failedTestCases = testCaseValidations.stream().filter(v -> "FAIL".equals(v.getStatus())).count();
        score -= failedTestCases * 10;
        
        // 难度评估问题扣分
        if (difficultyAssessment.getAssessment().contains("不准确")) {
            score -= 10;
        }
        
        return Math.max(0, score);
    }
    
    private List<String> generateImprovementSuggestions(List<String> basicIssues, List<QualityReport.TestCaseValidation> testCaseValidations, QualityReport.DifficultyAssessment difficultyAssessment) {
        List<String> suggestions = new ArrayList<>();
        
        if (!basicIssues.isEmpty()) {
            suggestions.add("修复基础信息问题");
        }
        
        long failedTestCases = testCaseValidations.stream().filter(v -> "FAIL".equals(v.getStatus())).count();
        if (failedTestCases > 0) {
            suggestions.add("修复失败的测试用例");
        }
        
        if (testCaseValidations.size() < 3) {
            suggestions.add("增加更多测试用例");
        }
        
        if (difficultyAssessment.getAssessment().contains("不准确")) {
            suggestions.add("调整难度等级");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("内容质量良好，继续保持");
        }
        
        return suggestions;
    }
}
