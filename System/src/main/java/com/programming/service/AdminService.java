package com.programming.service;

import com.programming.entity.ClassInfo;
import com.programming.entity.KnowledgePoint;
import com.programming.entity.LearningPath;
import com.programming.entity.Permission;
import com.programming.entity.ProblemSupportedLanguage;
import com.programming.entity.ReferenceSolution;
import com.programming.entity.Role;
import com.programming.entity.SystemLog;
import com.programming.entity.User;
import com.programming.vo.BatchImportProblemsVO;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> getUserList(int page, int size);

    Long addProblem(Map<String, Object> params);

    void addProblemWithCheck(Map<String, Object> params);

    Long updateProblem(Map<String, Object> params);

    List<ProblemSupportedLanguage> getProblemLanguages(Long problemId);

    void saveProblemLanguages(Long problemId, List<Map<String, Object>> params);

    List<ReferenceSolution> getProblemReferenceSolutions(Long problemId);

    void saveProblemReferenceSolutions(Long problemId, List<Map<String, Object>> params);

    void deleteProblem(Long id);

    Map<String, Object> getSubmitList(int page, int size);

    Map<String, Object> getStatistics();

    void batchImportProblems(BatchImportProblemsVO vo);

    List<Role> getRoleList();

    Role getRoleById(Long id);

    void addRole(Role role);

    void updateRole(Role role);

    void deleteRole(Long id);

    List<Permission> getPermissionList();

    void assignPermissions(Long roleId, List<Long> permissionIds);

    void updateUserStatus(Long userId, Integer status);

    void updateUserRole(Long userId, Long roleId);

    List<ClassInfo> getClassList();

    void addClass(ClassInfo classInfo);

    void updateClass(ClassInfo classInfo);

    void deleteClass(Long id);

    void addUserToClass(Long userId, Long classId, String role);

    void removeUserFromClass(Long userId, Long classId);

    List<User> getClassUsers(Long classId);

    Map<String, Object> getAuditLogs(int page, int size);

    Map<String, Object> getSystemStatus();

    Map<String, Object> getApiMetrics();

    Map<String, Object> getSandboxStatus();

    List<SystemLog> getSystemLogs(int page, int size);

    void addKnowledgePoint(KnowledgePoint knowledgePoint);

    void updateKnowledgePoint(KnowledgePoint knowledgePoint);

    void deleteKnowledgePoint(Long id);

    List<KnowledgePoint> getKnowledgePoints();

    void addLearningPath(LearningPath learningPath);

    void updateLearningPath(LearningPath learningPath);

    void deleteLearningPath(Long id);

    List<LearningPath> getLearningPaths();

    Map<String, Object> getDashboardData();

    Map<String, Object> getUserRetentionData();

    Map<String, Object> getKnowledgeDistribution();

    void exportStatistics(String type, String format);

    Map<String, Object> getHomeStatistics();
}
