package com.programming.service.impl;

import com.programming.entity.*;
import com.programming.mapper.*;
import com.programming.service.AdminService;
import com.programming.vo.BatchImportProblemsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private LearnRecordMapper learnRecordMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private ClassInfoMapper classInfoMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private LearningPathMapper learningPathMapper;

    @Autowired
    private SystemMonitorMapper systemMonitorMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public Map<String, Object> getUserList(int page, int size) {
        int offset = (page - 1) * size;
        List<User> users = userMapper.findByPage(offset, size);
        int total = userMapper.count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", users);

        return result;
    }

    @Override
    public void addProblem(Map<String, Object> params) {
        Problem problem = new Problem();
        problem.setTitle((String) params.get("title"));
        problem.setContent((String) params.get("content"));
        problem.setInput((String) params.get("input"));
        problem.setOutput((String) params.get("output"));
        problem.setDifficulty(((Number) params.get("difficulty")).intValue());
        problem.setLanguage((String) params.get("language"));
        
        if (params.get("timeLimit") != null) {
            problem.setTimeLimit(((Number) params.get("timeLimit")).intValue());
        }
        if (params.get("memoryLimit") != null) {
            problem.setMemoryLimit(((Number) params.get("memoryLimit")).intValue());
        }
        if (params.get("tags") != null) {
            problem.setTags((String) params.get("tags"));
        }
        if (params.get("knowledgePoints") != null) {
            problem.setKnowledgePoints((String) params.get("knowledgePoints"));
        }
        if (params.get("hints") != null) {
            problem.setHints((String) params.get("hints"));
        }
        if (params.get("sampleExplanation") != null) {
            problem.setSampleExplanation((String) params.get("sampleExplanation"));
        }
        
        problemMapper.insert(problem);
    }

    @Override
    @Transactional
    public void addProblemWithCheck(Map<String, Object> params) {
        String title = (String) params.get("title");
        
        Problem existingProblem = problemMapper.findByTitle(title);
        if (existingProblem != null) {
            throw new RuntimeException("题目已存在：" + title);
        }
        
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setContent((String) params.get("content"));
        problem.setInput((String) params.get("input"));
        problem.setOutput((String) params.get("output"));
        problem.setDifficulty(((Number) params.get("difficulty")).intValue());
        problem.setLanguage((String) params.get("language"));
        
        if (params.get("timeLimit") != null) {
            problem.setTimeLimit(((Number) params.get("timeLimit")).intValue());
        }
        if (params.get("memoryLimit") != null) {
            problem.setMemoryLimit(((Number) params.get("memoryLimit")).intValue());
        }
        if (params.get("tags") != null) {
            problem.setTags((String) params.get("tags"));
        }
        if (params.get("knowledgePoints") != null) {
            problem.setKnowledgePoints((String) params.get("knowledgePoints"));
        }
        if (params.get("hints") != null) {
            problem.setHints((String) params.get("hints"));
        }
        if (params.get("sampleExplanation") != null) {
            problem.setSampleExplanation((String) params.get("sampleExplanation"));
        }
        
        problemMapper.insert(problem);
        
        log.info("添加题目成功：{}", title);
    }

    @Override
    public void updateProblem(Map<String, Object> params) {
        Problem problem = new Problem();
        problem.setId(((Number) params.get("id")).longValue());
        problem.setTitle((String) params.get("title"));
        problem.setContent((String) params.get("content"));
        problem.setInput((String) params.get("input"));
        problem.setOutput((String) params.get("output"));
        problem.setDifficulty(((Number) params.get("difficulty")).intValue());
        problem.setLanguage((String) params.get("language"));
        
        if (params.get("timeLimit") != null) {
            problem.setTimeLimit(((Number) params.get("timeLimit")).intValue());
        }
        if (params.get("memoryLimit") != null) {
            problem.setMemoryLimit(((Number) params.get("memoryLimit")).intValue());
        }
        if (params.get("tags") != null) {
            problem.setTags((String) params.get("tags"));
        }
        if (params.get("knowledgePoints") != null) {
            problem.setKnowledgePoints((String) params.get("knowledgePoints"));
        }
        if (params.get("hints") != null) {
            problem.setHints((String) params.get("hints"));
        }
        if (params.get("sampleExplanation") != null) {
            problem.setSampleExplanation((String) params.get("sampleExplanation"));
        }
        
        problemMapper.update(problem);
    }

    @Override
    public void deleteProblem(Long id) {
        problemMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getSubmitList(int page, int size) {
        int offset = (page - 1) * size;
        List<Submit> submits = submitMapper.findAll(offset, size);
        int total = submitMapper.countAll();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", submits);

        return result;
    }

    @Override
    public Map<String, Object> getStatistics() {
        int userCount = userMapper.count();
        int problemCount = problemMapper.count(null, null, null);
        int submitCount = submitMapper.countAll();
        
        List<Submit> allSubmits = submitMapper.findAll(0, submitCount);
        int correctCount = 0;
        for (Submit submit : allSubmits) {
            if (submit.getResult() == 0) {
                correctCount++;
            }
        }
        
        String correctRate = "0.00%";
        if (submitCount > 0) {
            correctRate = String.format("%.2f%%", (double) correctCount / submitCount * 100);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userCount);
        result.put("problemCount", problemCount);
        result.put("submitCount", submitCount);
        result.put("correctCount", correctCount);
        result.put("correctRate", correctRate);

        return result;
    }

    @Override
    @Transactional
    public void batchImportProblems(BatchImportProblemsVO vo) {
        if (vo.getProblems() == null || vo.getProblems().isEmpty()) {
            throw new RuntimeException("题目列表不能为空");
        }

        int successCount = 0;
        int skipCount = 0;

        for (BatchImportProblemsVO.ProblemItem item : vo.getProblems()) {
            Problem existingProblem = problemMapper.findByTitle(item.getTitle());
            if (existingProblem != null) {
                log.warn("题目已存在，跳过：{}", item.getTitle());
                skipCount++;
                continue;
            }

            Problem problem = new Problem();
            problem.setTitle(item.getTitle());
            problem.setContent(item.getContent());
            problem.setInput(item.getInput());
            problem.setOutput(item.getOutput());
            problem.setDifficulty(item.getDifficulty());
            problem.setLanguage(item.getLanguage());
            
            if (item.getTimeLimit() != null) {
                problem.setTimeLimit(item.getTimeLimit());
            }
            if (item.getMemoryLimit() != null) {
                problem.setMemoryLimit(item.getMemoryLimit());
            }
            if (item.getTags() != null) {
                problem.setTags(item.getTags());
            }
            if (item.getKnowledgePoints() != null) {
                problem.setKnowledgePoints(item.getKnowledgePoints());
            }
            if (item.getHints() != null) {
                problem.setHints(item.getHints());
            }
            if (item.getSampleExplanation() != null) {
                problem.setSampleExplanation(item.getSampleExplanation());
            }
            
            problemMapper.insert(problem);
            successCount++;
        }

        log.info("批量导入完成，成功{}道，跳过{}道", successCount, skipCount);
    }

    // 角色和权限管理
    @Override
    public List<Role> getRoleList() {
        return roleMapper.findAll();
    }

    @Override
    public Role getRoleById(Long id) {
        return roleMapper.findById(id);
    }

    @Override
    public void addRole(Role role) {
        roleMapper.insert(role);
    }

    @Override
    public void updateRole(Role role) {
        roleMapper.update(role);
    }

    @Override
    public void deleteRole(Long id) {
        roleMapper.delete(id);
    }

    @Override
    public List<Permission> getPermissionList() {
        return permissionMapper.findAll();
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有的权限关联
        roleMapper.deleteRolePermissions(roleId);
        // 添加新的权限关联
        for (Long permissionId : permissionIds) {
            roleMapper.insertRolePermission(roleId, permissionId);
        }
    }

    // 用户账号状态管理
    @Override
    public void updateUserStatus(Long userId, Integer status) {
        userMapper.updateStatus(userId, status);
    }

    @Override
    public void updateUserRole(Long userId, Long roleId) {
        userMapper.updateRole(userId, roleId);
    }

    // 班级和学员管理
    @Override
    public List<ClassInfo> getClassList() {
        return classInfoMapper.findAll();
    }

    @Override
    public void addClass(ClassInfo classInfo) {
        classInfoMapper.insert(classInfo);
    }

    @Override
    public void updateClass(ClassInfo classInfo) {
        classInfoMapper.update(classInfo);
    }

    @Override
    public void deleteClass(Long id) {
        classInfoMapper.delete(id);
    }

    @Override
    public void addUserToClass(Long userId, Long classId, String role) {
        classInfoMapper.insertUserClass(userId, classId, role);
    }

    @Override
    public void removeUserFromClass(Long userId, Long classId) {
        classInfoMapper.deleteUserClass(userId, classId);
    }

    @Override
    public List<User> getClassUsers(Long classId) {
        return classInfoMapper.findUsersByClassId(classId);
    }

    // 操作审计日志
    @Override
    public Map<String, Object> getAuditLogs(int page, int size) {
        int offset = (page - 1) * size;
        List<AuditLog> logs = auditLogMapper.findPage(offset, size);
        int total = auditLogMapper.count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", logs);
        return result;
    }

    // 系统监控
    @Override
    public Map<String, Object> getSystemStatus() {
        SystemMonitor monitor = systemMonitorMapper.getLatest();
        Map<String, Object> result = new HashMap<>();
        if (monitor != null) {
            result.put("metricName", monitor.getMetricName());
            result.put("metricValue", monitor.getMetricValue());
            result.put("metricUnit", monitor.getMetricUnit());
            result.put("createTime", monitor.getCreateTime());
        }
        return result;
    }

    @Override
    public Map<String, Object> getApiMetrics() {
        List<ApiMetric> metrics = systemMonitorMapper.getApiMetrics();
        Map<String, Object> result = new HashMap<>();
        result.put("metrics", metrics);
        return result;
    }

    @Override
    public Map<String, Object> getSandboxStatus() {
        List<SandboxStatus> statuses = systemMonitorMapper.getSandboxStatus();
        Map<String, Object> result = new HashMap<>();
        result.put("statuses", statuses);
        return result;
    }

    @Override
    public List<SystemLog> getSystemLogs(int page, int size) {
        int offset = (page - 1) * size;
        return systemMonitorMapper.getSystemLogs(offset, size);
    }

    // 内容管理
    @Override
    public void addKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.insert(knowledgePoint);
    }

    @Override
    public void updateKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.update(knowledgePoint);
    }

    @Override
    public void deleteKnowledgePoint(Long id) {
        knowledgePointMapper.delete(id);
    }

    @Override
    public List<KnowledgePoint> getKnowledgePoints() {
        return knowledgePointMapper.findAll();
    }

    @Override
    public void addLearningPath(LearningPath learningPath) {
        learningPathMapper.insertPath(learningPath);
    }

    @Override
    public void updateLearningPath(LearningPath learningPath) {
        learningPathMapper.updatePath(learningPath);
    }

    @Override
    public void deleteLearningPath(Long id) {
        learningPathMapper.deletePath(id);
    }

    @Override
    public List<LearningPath> getLearningPaths() {
        return learningPathMapper.selectAllPaths();
    }

    // 数据分析
    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userMapper.count());
        result.put("problemCount", problemMapper.count(null, null, null));
        result.put("submitCount", submitMapper.countAll());
        result.put("activeUsers", userMapper.countActiveUsers());
        return result;
    }

    @Override
    public Map<String, Object> getHomeStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userMapper.count());
        result.put("problemCount", problemMapper.count(null, null, null));
        result.put("pathCount", learningPathMapper.selectAllPaths().size());
        return result;
    }

    @Override
    public Map<String, Object> getUserRetentionData() {
        // 这里需要实现用户留存数据的查询逻辑
        Map<String, Object> result = new HashMap<>();
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        return result;
    }

    @Override
    public Map<String, Object> getKnowledgeDistribution() {
        // 这里需要实现知识点分布数据的查询逻辑
        Map<String, Object> result = new HashMap<>();
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        return result;
    }

    @Override
    public void exportStatistics(String type, String format) {
        // 这里需要实现数据导出逻辑
        log.info("导出统计数据，类型：{}，格式：{}", type, format);
        // 暂时只是记录日志，实际项目中需要根据具体需求实现
    }
}