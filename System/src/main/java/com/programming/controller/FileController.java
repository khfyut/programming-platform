package com.programming.controller;

import com.programming.service.AdminService;
import com.programming.util.ResultUtil;
import com.programming.util.ExcelUtil;
import com.programming.vo.BatchImportProblemsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class FileController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ExcelUtil excelUtil;

    @PostMapping("/problem/import-excel")
    public ResultUtil<Void> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            BatchImportProblemsVO vo = excelUtil.parseExcelFile(file);
            adminService.batchImportProblems(vo);
            return ResultUtil.success(null);
        } catch (Exception e) {
            log.error("Excel文件导入失败", e);
            return ResultUtil.error("Excel文件导入失败：" + e.getMessage());
        }
    }

    @PostMapping("/problem/import-csv")
    public ResultUtil<Void> importFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            BatchImportProblemsVO vo = excelUtil.parseCsvFile(file);
            adminService.batchImportProblems(vo);
            return ResultUtil.success(null);
        } catch (Exception e) {
            log.error("CSV文件导入失败", e);
            return ResultUtil.error("CSV文件导入失败：" + e.getMessage());
        }
    }
}