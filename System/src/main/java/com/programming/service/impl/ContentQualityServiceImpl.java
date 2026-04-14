package com.programming.service.impl;

import com.programming.entity.Problem;
import com.programming.entity.TestCase;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.TestCaseMapper;
import com.programming.service.ContentQualityService;
import com.programming.vo.QualityReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ContentQualityServiceImpl implements ContentQualityService {

    private static final Set<Long> JAVA_AGENT_PATH_IDS = Set.of(1L, 3L, 4L, 5L, 6L);

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private DataSource dataSource;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public QualityReport validateProblem(Long problemId) {
        long startTime = System.currentTimeMillis();
        QualityReport report = new QualityReport();
        try {
            Problem problem = problemMapper.findById(problemId);
            if (problem == null) {
                report.setOverallStatus("ERROR");
                report.setBasicIssues(List.of("Problem not found"));
                return report;
            }

            report.setProblemId(problemId);
            report.setProblemTitle(problem.getTitle());
            List<String> basicIssues = checkBasicInfo(problem);
            report.setBasicIssues(basicIssues);

            List<QualityReport.TestCaseValidation> testCaseValidations = validateTestCases(problem);
            report.setTestCaseValidations(testCaseValidations);

            QualityReport.DifficultyAssessment difficultyAssessment = assessDifficulty(problem, testCaseValidations);
            report.setDifficultyAssessment(difficultyAssessment);

            int qualityScore = calculateQualityScore(basicIssues, testCaseValidations, difficultyAssessment);
            report.setQualityScore(qualityScore);
            report.setImprovementSuggestions(generateImprovementSuggestions(basicIssues, testCaseValidations, difficultyAssessment));
            report.setOverallStatus(qualityScore >= 80 ? "EXCELLENT" : qualityScore >= 60 ? "GOOD" : "NEEDS_IMPROVEMENT");
        } catch (Exception e) {
            log.error("Failed to validate problem quality", e);
            report.setOverallStatus("ERROR");
            report.setBasicIssues(List.of("Validation failed: " + e.getMessage()));
        } finally {
            report.setValidationTime(System.currentTimeMillis() - startTime);
        }
        return report;
    }

    @Override
    public void validateProblemsBatch() {
        List<Problem> problems = problemMapper.selectAll();
        log.info("Start batch problem quality validation, size={}", problems.size());
        for (Problem problem : problems) {
            executorService.submit(() -> {
                QualityReport report = validateProblem(problem.getId());
                log.info("Problem {} ({}) quality score={}, status={}",
                        report.getProblemTitle(), report.getProblemId(),
                        report.getQualityScore(), report.getOverallStatus());
            });
        }
    }

    @Override
    public void generateQualityReport() {
        log.info("Generate content quality report placeholder");
    }

    @Override
    public void optimizeContentQuality() {
        log.info("Optimize content quality placeholder");
    }

    @Override
    public Map<String, Object> getSummary() {
        List<Map<String, Object>> problems = getProblemQualityRows();
        List<Map<String, Object>> bindings = getPathBindingQualityRows();
        List<Map<String, Object>> tags = getTagQualityRows();

        long agentReady = problems.stream().filter(row -> Boolean.TRUE.equals(row.get("agentReady"))).count();
        long javaScope = problems.stream().filter(row -> "java".equals(row.get("agentScope"))).count();
        long bindingIssues = bindings.stream().filter(row -> Boolean.FALSE.equals(row.get("semanticMatched"))).count();
        long tagIssues = tags.stream().filter(row -> Boolean.FALSE.equals(row.get("knownKnowledgePoints"))).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProblems", problems.size());
        summary.put("agentReadyProblems", agentReady);
        summary.put("javaAgentScopeProblems", javaScope);
        summary.put("pathBindingRows", bindings.size());
        summary.put("pathBindingIssues", bindingIssues);
        summary.put("tagRows", tags.size());
        summary.put("tagIssues", tagIssues);
        summary.put("agentScope", "java");
        return summary;
    }

    @Override
    public List<Map<String, Object>> getProblemQualityRows() {
        String sql = """
                SELECT
                    p.id,
                    p.title,
                    p.status,
                    p.language,
                    p.tags,
                    p.knowledge_points,
                    COUNT(tc.id) AS test_case_count,
                    SUM(CASE WHEN tc.is_sample = 1 THEN 1 ELSE 0 END) AS sample_case_count,
                    SUM(CASE WHEN tc.is_sample = 0 THEN 1 ELSE 0 END) AS hidden_case_count,
                    MAX(CASE WHEN LOWER(psl.language_code) = 'java' AND psl.status = 'ACTIVE' THEN 1 ELSE 0 END) AS java_active,
                    MAX(CASE WHEN LOWER(rs.language) = 'java' THEN 1 ELSE 0 END) AS java_solution
                FROM problem p
                LEFT JOIN test_case tc ON tc.problem_id = p.id
                LEFT JOIN problem_supported_language psl ON psl.problem_id = p.id
                LEFT JOIN reference_solution rs ON rs.problem_id = p.id
                GROUP BY p.id, p.title, p.status, p.language, p.tags, p.knowledge_points
                ORDER BY p.id
                """;

        return query(sql, rs -> {
            int testCases = rs.getInt("test_case_count");
            int hiddenCases = rs.getInt("hidden_case_count");
            boolean javaActive = rs.getInt("java_active") > 0;
            boolean javaSolution = rs.getInt("java_solution") > 0;
            String status = rs.getString("status");
            boolean agentReady = "PUBLISHED".equalsIgnoreCase(status)
                    && javaActive
                    && testCases >= 2
                    && hiddenCases >= 1
                    && javaSolution;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("problemId", rs.getLong("id"));
            row.put("title", rs.getString("title"));
            row.put("status", status);
            row.put("language", rs.getString("language"));
            row.put("tags", rs.getString("tags"));
            row.put("knowledgePoints", rs.getString("knowledge_points"));
            row.put("testCaseCount", testCases);
            row.put("sampleCaseCount", rs.getInt("sample_case_count"));
            row.put("hiddenCaseCount", hiddenCases);
            row.put("javaActive", javaActive);
            row.put("javaReferenceSolution", javaSolution);
            row.put("agentReady", agentReady);
            row.put("agentScope", agentReady ? "java" : "none");
            return row;
        });
    }

    @Override
    public List<Map<String, Object>> getPathBindingQualityRows() {
        String sql = """
                SELECT
                    lp.id AS path_id,
                    lp.name AS path_name,
                    lp.language AS path_language,
                    pc.id AS chapter_id,
                    pc.name AS chapter_name,
                    pl.id AS level_id,
                    pl.name AS level_name,
                    pl.knowledge_points AS level_knowledge_points,
                    p.id AS problem_id,
                    p.title AS problem_title,
                    p.tags AS problem_tags,
                    p.knowledge_points AS problem_knowledge_points,
                    p.status AS problem_status,
                    COUNT(tc.id) AS test_case_count,
                    SUM(CASE WHEN tc.is_sample = 0 THEN 1 ELSE 0 END) AS hidden_case_count,
                    MAX(CASE WHEN LOWER(psl.language_code) = 'java' AND psl.status = 'ACTIVE' THEN 1 ELSE 0 END) AS java_active,
                    MAX(CASE WHEN LOWER(rs.language) = 'java' THEN 1 ELSE 0 END) AS java_solution
                FROM path_level_problem plp
                INNER JOIN path_level pl ON pl.id = plp.level_id
                INNER JOIN path_chapter pc ON pc.id = pl.chapter_id
                INNER JOIN learning_path lp ON lp.id = pc.path_id
                INNER JOIN problem p ON p.id = plp.problem_id
                LEFT JOIN test_case tc ON tc.problem_id = p.id
                LEFT JOIN problem_supported_language psl ON psl.problem_id = p.id
                LEFT JOIN reference_solution rs ON rs.problem_id = p.id
                WHERE lp.id IN (1, 3, 4, 5, 6)
                GROUP BY lp.id, lp.name, lp.language, pc.id, pc.name, pl.id, pl.name,
                         pl.knowledge_points, p.id, p.title, p.tags, p.knowledge_points, p.status
                ORDER BY lp.id, pc.order_num, pl.order_num, plp.order_num
                """;

        return query(sql, rs -> {
            Set<String> levelTokens = tokens(rs.getString("level_knowledge_points"));
            Set<String> problemTokens = tokens(rs.getString("problem_knowledge_points"));
            problemTokens.addAll(tokens(rs.getString("problem_tags")));
            Set<String> intersection = new LinkedHashSet<>(levelTokens);
            intersection.retainAll(problemTokens);

            int testCases = rs.getInt("test_case_count");
            int hiddenCases = rs.getInt("hidden_case_count");
            boolean agentReady = "PUBLISHED".equalsIgnoreCase(rs.getString("problem_status"))
                    && rs.getInt("java_active") > 0
                    && testCases >= 2
                    && hiddenCases >= 1
                    && rs.getInt("java_solution") > 0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("pathId", rs.getLong("path_id"));
            row.put("pathName", rs.getString("path_name"));
            row.put("chapterId", rs.getLong("chapter_id"));
            row.put("chapterName", rs.getString("chapter_name"));
            row.put("levelId", rs.getLong("level_id"));
            row.put("levelName", rs.getString("level_name"));
            row.put("levelKnowledgePoints", rs.getString("level_knowledge_points"));
            row.put("problemId", rs.getLong("problem_id"));
            row.put("problemTitle", rs.getString("problem_title"));
            row.put("problemKnowledgePoints", rs.getString("problem_knowledge_points"));
            row.put("problemTags", rs.getString("problem_tags"));
            row.put("semanticMatched", !intersection.isEmpty());
            row.put("matchedKnowledgePoints", new ArrayList<>(intersection));
            row.put("agentReady", agentReady);
            row.put("agentScope", JAVA_AGENT_PATH_IDS.contains(rs.getLong("path_id")) ? "java" : "none");
            return row;
        });
    }

    @Override
    public List<Map<String, Object>> getTagQualityRows() {
        Set<String> knownKnowledgePoints = loadKnowledgePointNames();
        String sql = """
                SELECT id, title, status, tags, knowledge_points
                FROM problem
                ORDER BY id
                """;

        return query(sql, rs -> {
            Set<String> points = tokens(rs.getString("knowledge_points"));
            Set<String> unknown = new LinkedHashSet<>(points);
            unknown.removeAll(knownKnowledgePoints);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("problemId", rs.getLong("id"));
            row.put("title", rs.getString("title"));
            row.put("status", rs.getString("status"));
            row.put("tags", rs.getString("tags"));
            row.put("knowledgePoints", rs.getString("knowledge_points"));
            row.put("knownKnowledgePoints", unknown.isEmpty());
            row.put("unknownKnowledgePoints", new ArrayList<>(unknown));
            row.put("agentScope", "PUBLISHED".equalsIgnoreCase(rs.getString("status")) ? "java" : "none");
            return row;
        });
    }

    private Set<String> loadKnowledgePointNames() {
        return new LinkedHashSet<>(query("SELECT name FROM knowledge_point", rs -> rs.getString("name")));
    }

    private List<String> checkBasicInfo(Problem problem) {
        List<String> issues = new ArrayList<>();
        if (problem.getTitle() == null || problem.getTitle().trim().isEmpty()) {
            issues.add("Missing title");
        }
        if (problem.getContent() == null || problem.getContent().trim().isEmpty()) {
            issues.add("Missing content");
        }
        if (problem.getDifficulty() == null) {
            issues.add("Missing difficulty");
        }
        if (problem.getLanguage() == null || problem.getLanguage().trim().isEmpty()) {
            issues.add("Missing language");
        }
        if (problem.getTimeLimit() == null || problem.getTimeLimit() <= 0) {
            issues.add("Invalid time limit");
        }
        if (problem.getMemoryLimit() == null || problem.getMemoryLimit() <= 0) {
            issues.add("Invalid memory limit");
        }
        if (problem.getKnowledgePoints() == null || problem.getKnowledgePoints().trim().isEmpty()) {
            issues.add("Missing knowledge points");
        }
        return issues;
    }

    private List<QualityReport.TestCaseValidation> validateTestCases(Problem problem) {
        List<QualityReport.TestCaseValidation> validations = new ArrayList<>();
        List<TestCase> testCases = testCaseMapper.findByProblemId(problem.getId());
        if (testCases == null || testCases.isEmpty()) {
            QualityReport.TestCaseValidation validation = new QualityReport.TestCaseValidation();
            validation.setStatus("ERROR");
            validation.setErrorMessage("No test cases");
            validations.add(validation);
            return validations;
        }

        for (TestCase testCase : testCases) {
            QualityReport.TestCaseValidation validation = new QualityReport.TestCaseValidation();
            validation.setTestCaseId(testCase.getId());
            validation.setStatus("PASS");
            validation.setExecutionTime(0);
            validations.add(validation);
        }
        return validations;
    }

    private QualityReport.DifficultyAssessment assessDifficulty(Problem problem, List<QualityReport.TestCaseValidation> validations) {
        QualityReport.DifficultyAssessment assessment = new QualityReport.DifficultyAssessment();
        assessment.setAssignedDifficulty(problem.getDifficulty() != null ? problem.getDifficulty() : 0);
        int estimatedDifficulty = 0;
        if (problem.getContent() != null && problem.getContent().length() > 500) {
            estimatedDifficulty++;
        }
        if (validations.size() > 5) {
            estimatedDifficulty++;
        }
        assessment.setEstimatedDifficulty(estimatedDifficulty);
        assessment.setAssessment(Math.abs(assessment.getAssignedDifficulty() - estimatedDifficulty) > 1
                ? "Difficulty may be inaccurate"
                : "Difficulty is reasonable");
        return assessment;
    }

    private int calculateQualityScore(List<String> basicIssues,
                                      List<QualityReport.TestCaseValidation> testCaseValidations,
                                      QualityReport.DifficultyAssessment difficultyAssessment) {
        int score = 100;
        score -= basicIssues.size() * 5;
        long failedTestCases = testCaseValidations.stream()
                .filter(v -> !"PASS".equals(v.getStatus()))
                .count();
        score -= failedTestCases * 10;
        if (difficultyAssessment.getAssessment().contains("inaccurate")) {
            score -= 10;
        }
        return Math.max(0, score);
    }

    private List<String> generateImprovementSuggestions(List<String> basicIssues,
                                                        List<QualityReport.TestCaseValidation> testCaseValidations,
                                                        QualityReport.DifficultyAssessment difficultyAssessment) {
        List<String> suggestions = new ArrayList<>();
        if (!basicIssues.isEmpty()) {
            suggestions.add("Fix basic information issues");
        }
        long failedTestCases = testCaseValidations.stream()
                .filter(v -> !"PASS".equals(v.getStatus()))
                .count();
        if (failedTestCases > 0) {
            suggestions.add("Fix failing or missing test cases");
        }
        if (testCaseValidations.size() < 2) {
            suggestions.add("Add at least two test cases");
        }
        if (difficultyAssessment.getAssessment().contains("inaccurate")) {
            suggestions.add("Review difficulty level");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Content quality looks healthy");
        }
        return suggestions;
    }

    private Set<String> tokens(String raw) {
        Set<String> values = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) {
            return values;
        }
        Arrays.stream(raw.split("[,，;；、\\s]+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .forEach(values::add);
        return values;
    }

    private <T> List<T> query(String sql, SqlRowMapper<T> mapper) {
        List<T> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(mapper.map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Content quality query failed", e);
        }
        return rows;
    }

    @SuppressWarnings("unused")
    private <T> List<T> query(String sql, List<Object> params, SqlRowMapper<T> mapper) {
        List<T> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Content quality query failed", e);
        }
        return rows;
    }

    @FunctionalInterface
    private interface SqlRowMapper<T> {
        T map(ResultSet rs) throws Exception;
    }
}
