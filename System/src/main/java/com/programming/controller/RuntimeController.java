package com.programming.controller;

import com.programming.service.runtime.RuntimeCatalogService;
import com.programming.service.runtime.RuntimeLanguageDefinition;
import com.programming.util.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/runtime")
public class RuntimeController {

    private final RuntimeCatalogService runtimeCatalogService;

    public RuntimeController(RuntimeCatalogService runtimeCatalogService) {
        this.runtimeCatalogService = runtimeCatalogService;
    }

    @GetMapping("/languages")
    public ResultUtil<List<Map<String, Object>>> getLanguages() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RuntimeLanguageDefinition definition : runtimeCatalogService.getJudgeLanguages()) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", definition.getCode());
            item.put("label", definition.getLabel());
            item.put("monacoLanguage", definition.getMonacoLanguage());
            item.put("defaultFileName", definition.getDefaultFileName());
            item.put("defaultStarterCode", definition.getDefaultStarterCode());
            item.put("supportsJudge", definition.isSupportsJudge());
            item.put("enabled", definition.isEnabled());
            item.put("sortOrder", definition.getSortOrder());
            result.add(item);
        }
        return ResultUtil.success(result);
    }
}
