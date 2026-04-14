package com.programming.controller;

import com.programming.service.ContentQualityService;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/content-quality")
public class ContentQualityController {

    @Autowired
    private ContentQualityService contentQualityService;

    @GetMapping("/summary")
    public ResultUtil<Map<String, Object>> getSummary() {
        return ResultUtil.success(contentQualityService.getSummary());
    }

    @GetMapping("/problems")
    public ResultUtil<List<Map<String, Object>>> getProblems() {
        return ResultUtil.success(contentQualityService.getProblemQualityRows());
    }

    @GetMapping("/path-bindings")
    public ResultUtil<List<Map<String, Object>>> getPathBindings() {
        return ResultUtil.success(contentQualityService.getPathBindingQualityRows());
    }

    @GetMapping("/tags")
    public ResultUtil<List<Map<String, Object>>> getTags() {
        return ResultUtil.success(contentQualityService.getTagQualityRows());
    }
}
