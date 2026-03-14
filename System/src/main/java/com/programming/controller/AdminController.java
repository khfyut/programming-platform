package com.programming.controller;

import com.programming.service.AdminService;
import com.programming.util.ResultUtil;
import com.programming.vo.BatchImportProblemsVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/problem/batch-import")
    public ResultUtil<Void> batchImportProblems(@RequestBody BatchImportProblemsVO vo) {
        try {
            adminService.batchImportProblems(vo);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}