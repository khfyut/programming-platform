package com.programming.service;

import com.programming.entity.*;
import com.programming.vo.BatchImportProblemsVO;
import java.util.List;
import java.util.Map;

public interface AdminService {
    // 基础功能
    Map<String, Object> getUserList(int page, int size);
    void addProblem(Map<String, Object> params);
    void addProblemWithCheck(Map<String, Object> params);
    void updateProblem(Map<String, Object> params);
    void deleteProblem(Long id);
    Map<String, Object> getSubmitList(int page, int size);
    Map<String, Object> getStatistics();
    void batchImportProblems(BatchImportProblemsVO vo);
    
    // 角色和权限管理
    List<Role> getRoleList();
    Role getRoleById(Long id);
    void addRole(Role role);
    void updateRole(Role role);
    void deleteRole(Long id);
    List<Permission> getPermissionList();
    void assignPermissions(Long roleId, List<Long> permissionIds);
    
    // 用户账号状态管理
    void updateUserStatus(Long userId, Integer status);
    void updateUserRole(Long userId, Long roleId);
    
    // 班级和学员管理
    List<ClassInfo> getClassList();
    void addClass(ClassInfo classInfo);
    void updateClass(ClassInfo classInfo);
    void deleteClass(Long id);
    void addUserToClass(Long userId, Long classId, String role);
    void removeUserFromClass(Long userId, Long classId);
    List<User> getClassUsers(Long classId);
    
    // 操作审计日志
    Map<String, Object> getAuditLogs(int page, int size);
    
    // 系统监控
    Map<String, Object> getSystemStatus();
    Map<String, Object> getApiMetrics();
    Map<String, Object> getSandboxStatus();
    List<SystemLog> getSystemLogs(int page, int size);
    
    // 内容管理
    void addKnowledgePoint(KnowledgePoint knowledgePoint);
    void updateKnowledgePoint(KnowledgePoint knowledgePoint);
    void deleteKnowledgePoint(Long id);
    List<KnowledgePoint> getKnowledgePoints();
    void addLearningPath(LearningPath learningPath);
    void updateLearningPath(LearningPath learningPath);
    void deleteLearningPath(Long id);
    List<LearningPath> getLearningPaths();
    
    // 数据分析
    Map<String, Object> getDashboardData();
    Map<String, Object> getUserRetentionData();
    Map<String, Object> getKnowledgeDistribution();
    void exportStatistics(String type, String format);
    
    // 首页统计
    Map<String, Object> getHomeStatistics();
}