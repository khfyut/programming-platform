package com.programming.controller;

import com.programming.entity.*;
import com.programming.service.AdminService;
import com.programming.util.ResultUtil;
import com.programming.vo.BatchImportProblemsVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/user/list")
    public ResultUtil<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResultUtil.success(adminService.getUserList(page, size));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/user/status")
    public ResultUtil<Void> updateUserStatus(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            adminService.updateUserStatus(userId, status);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/user/role")
    public ResultUtil<Void> updateUserRole(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long roleId = Long.valueOf(params.get("roleId").toString());
            adminService.updateUserRole(userId, roleId);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/problem/add")
    public ResultUtil<Void> addProblem(@RequestBody Map<String, Object> params) {
        try {
            adminService.addProblem(params);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/problem/add-with-check")
    public ResultUtil<Void> addProblemWithCheck(@RequestBody Map<String, Object> params) {
        try {
            adminService.addProblemWithCheck(params);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/problem/update")
    public ResultUtil<Void> updateProblem(@RequestBody Map<String, Object> params) {
        try {
            adminService.updateProblem(params);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/problem/delete/{id}")
    public ResultUtil<Void> deleteProblem(@PathVariable Long id) {
        try {
            adminService.deleteProblem(id);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/submit/list")
    public ResultUtil<Map<String, Object>> getSubmitList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResultUtil.success(adminService.getSubmitList(page, size));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResultUtil<Map<String, Object>> getStatistics() {
        try {
            return ResultUtil.success(adminService.getStatistics());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/home/statistics")
    public ResultUtil<Map<String, Object>> getHomeStatistics() {
        try {
            return ResultUtil.success(adminService.getHomeStatistics());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/problem/batch-import")
    public ResultUtil<Void> batchImportProblems(@RequestBody BatchImportProblemsVO vo) {
        try {
            adminService.batchImportProblems(vo);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/role/list")
    public ResultUtil<List<Role>> getRoleList() {
        try {
            return ResultUtil.success(adminService.getRoleList());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/role/{id}")
    public ResultUtil<Role> getRoleById(@PathVariable Long id) {
        try {
            return ResultUtil.success(adminService.getRoleById(id));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/role/add")
    public ResultUtil<Void> addRole(@RequestBody Role role) {
        try {
            adminService.addRole(role);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/role/update")
    public ResultUtil<Void> updateRole(@RequestBody Role role) {
        try {
            adminService.updateRole(role);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/role/delete/{id}")
    public ResultUtil<Void> deleteRole(@PathVariable Long id) {
        try {
            adminService.deleteRole(id);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/permission/list")
    public ResultUtil<List<Permission>> getPermissionList() {
        try {
            return ResultUtil.success(adminService.getPermissionList());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/role/permission")
    public ResultUtil<Void> assignPermissions(@RequestBody Map<String, Object> params) {
        try {
            Long roleId = Long.valueOf(params.get("roleId").toString());
            @SuppressWarnings("unchecked")
            List<Integer> permissionIds = (List<Integer>) params.get("permissionIds");
            List<Long> ids = permissionIds.stream().map(Long::valueOf).toList();
            adminService.assignPermissions(roleId, ids);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/class/list")
    public ResultUtil<List<ClassInfo>> getClassList() {
        try {
            return ResultUtil.success(adminService.getClassList());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/class/add")
    public ResultUtil<Void> addClass(@RequestBody ClassInfo classInfo) {
        try {
            adminService.addClass(classInfo);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/class/update")
    public ResultUtil<Void> updateClass(@RequestBody ClassInfo classInfo) {
        try {
            adminService.updateClass(classInfo);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/class/delete/{id}")
    public ResultUtil<Void> deleteClass(@PathVariable Long id) {
        try {
            adminService.deleteClass(id);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/class/{classId}/users")
    public ResultUtil<List<User>> getClassUsers(@PathVariable Long classId) {
        try {
            return ResultUtil.success(adminService.getClassUsers(classId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/class/user/add")
    public ResultUtil<Void> addUserToClass(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long classId = Long.valueOf(params.get("classId").toString());
            String role = (String) params.getOrDefault("role", "student");
            adminService.addUserToClass(userId, classId, role);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/class/user/remove")
    public ResultUtil<Void> removeUserFromClass(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long classId = Long.valueOf(params.get("classId").toString());
            adminService.removeUserFromClass(userId, classId);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/knowledge/list")
    public ResultUtil<List<KnowledgePoint>> getKnowledgePoints() {
        try {
            return ResultUtil.success(adminService.getKnowledgePoints());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/knowledge/add")
    public ResultUtil<Void> addKnowledgePoint(@RequestBody KnowledgePoint knowledgePoint) {
        try {
            adminService.addKnowledgePoint(knowledgePoint);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/knowledge/update")
    public ResultUtil<Void> updateKnowledgePoint(@RequestBody KnowledgePoint knowledgePoint) {
        try {
            adminService.updateKnowledgePoint(knowledgePoint);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/knowledge/delete/{id}")
    public ResultUtil<Void> deleteKnowledgePoint(@PathVariable Long id) {
        try {
            adminService.deleteKnowledgePoint(id);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/path/list")
    public ResultUtil<List<LearningPath>> getLearningPaths() {
        try {
            return ResultUtil.success(adminService.getLearningPaths());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/path/add")
    public ResultUtil<Void> addLearningPath(@RequestBody LearningPath learningPath) {
        try {
            adminService.addLearningPath(learningPath);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/path/update")
    public ResultUtil<Void> updateLearningPath(@RequestBody LearningPath learningPath) {
        try {
            adminService.updateLearningPath(learningPath);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/path/delete/{id}")
    public ResultUtil<Void> deleteLearningPath(@PathVariable Long id) {
        try {
            adminService.deleteLearningPath(id);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/audit/list")
    public ResultUtil<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResultUtil.success(adminService.getAuditLogs(page, size));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/system/status")
    public ResultUtil<Map<String, Object>> getSystemStatus() {
        try {
            return ResultUtil.success(adminService.getSystemStatus());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/system/metrics")
    public ResultUtil<Map<String, Object>> getApiMetrics() {
        try {
            return ResultUtil.success(adminService.getApiMetrics());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/system/sandbox")
    public ResultUtil<Map<String, Object>> getSandboxStatus() {
        try {
            return ResultUtil.success(adminService.getSandboxStatus());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/system/logs")
    public ResultUtil<List<SystemLog>> getSystemLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResultUtil.success(adminService.getSystemLogs(page, size));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResultUtil<Map<String, Object>> getDashboardData() {
        try {
            return ResultUtil.success(adminService.getDashboardData());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}