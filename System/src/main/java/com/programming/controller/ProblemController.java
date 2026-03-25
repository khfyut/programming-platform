package com.programming.controller;

import com.programming.entity.Problem;
import com.programming.entity.TestCase;
import com.programming.entity.User;
import com.programming.service.ProblemService;
import com.programming.service.TestCaseService;
import com.programming.service.UserService;
import com.programming.util.JwtUtil;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problem")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private UserService userService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public ResultUtil getProblemList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String knowledge,
            HttpServletRequest request) {
        try {
            // 获取当前登录用户ID
            Long userId = null;
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtUtil.validateToken(token)) {
                    userId = jwtUtil.getUserIdFromToken(token);
                }
            }
            
            return ResultUtil.success(problemService.getProblemList(page, size, difficulty, language, knowledge, userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResultUtil<Problem> getProblemDetail(@PathVariable Long id) {
        try {
            Problem problem = problemService.getProblemDetail(id);
            List<TestCase> testCases = testCaseService.getTestCasesByProblemId(id);
            problem.setTestCases(testCases);
            return ResultUtil.success(problem);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/test-cases/sample")
    public ResultUtil<List<TestCase>> getSampleTestCases(@PathVariable Long id) {
        try {
            List<TestCase> testCases = testCaseService.getSampleTestCasesByProblemId(id);
            return ResultUtil.success(testCases);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/test-cases/all")
    public ResultUtil<List<TestCase>> getAllTestCases(@PathVariable Long id) {
        try {
            List<TestCase> testCases = testCaseService.getTestCasesByProblemId(id);
            return ResultUtil.success(testCases);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/test-cases")
    public ResultUtil<TestCase> createTestCase(@PathVariable Long id, @RequestBody TestCase testCase) {
        try {
            testCase.setProblemId(id);
            TestCase created = testCaseService.createTestCase(testCase);
            return ResultUtil.success(created);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/test-cases/batch")
    public ResultUtil<List<TestCase>> batchCreateTestCases(@PathVariable Long id, @RequestBody List<TestCase> testCases) {
        try {
            List<TestCase> created = testCaseService.batchCreateTestCases(id, testCases);
            return ResultUtil.success(created);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping("/test-cases/{testCaseId}")
    public ResultUtil<TestCase> updateTestCase(@PathVariable Long testCaseId, @RequestBody TestCase testCase) {
        try {
            testCase.setId(testCaseId);
            TestCase updated = testCaseService.updateTestCase(testCase);
            return ResultUtil.success(updated);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/test-cases/{testCaseId}")
    public ResultUtil<Boolean> deleteTestCase(@PathVariable Long testCaseId) {
        try {
            boolean deleted = testCaseService.deleteTestCase(testCaseId);
            return ResultUtil.success(deleted);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/tag/{tag}")
    public ResultUtil<List<Problem>> getProblemsByTag(@PathVariable String tag) {
        try {
            List<Problem> problems = problemService.getProblemsByTag(tag);
            return ResultUtil.success(problems);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/languages")
    public ResultUtil getLanguages() {
        try {
            return ResultUtil.success(problemService.getLanguages());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/test-jdbc")
    public ResultUtil<Map<String, Object>> testJdbcQuery() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            Connection conn = dataSource.getConnection();
            
            result.put("url", conn.getMetaData().getURL());
            result.put("user", conn.getMetaData().getUserName());
            result.put("database", conn.getCatalog());
            
            Statement stmt = conn.createStatement();
            
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) as cnt FROM problem");
            if (rs1.next()) {
                result.put("total", rs1.getInt("cnt"));
            }
            
            ResultSet rs2 = stmt.executeQuery("SELECT id, title FROM problem ORDER BY id DESC LIMIT 5");
            List<Map<String, Object>> problems = new ArrayList<>();
            while (rs2.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs2.getLong("id"));
                p.put("title", rs2.getString("title"));
                problems.add(p);
            }
            result.put("latestProblems", problems);
            
            rs1.close();
            rs2.close();
            stmt.close();
            conn.close();
            
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
