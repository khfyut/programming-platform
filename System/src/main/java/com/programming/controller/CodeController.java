package com.programming.controller;

import com.programming.service.CodeSandboxService;
import com.programming.service.ProblemService;
import com.programming.service.runtime.RuntimeCatalogService;
import com.programming.util.ResultUtil;
import com.programming.vo.CodeExecutionResult;
import com.programming.vo.CodeRunVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code")
public class CodeController {

    @Autowired
    private CodeSandboxService codeSandboxService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private RuntimeCatalogService runtimeCatalogService;

    @PostMapping("/run")
    public ResultUtil<CodeExecutionResult> runCode(@RequestBody CodeRunVO codeRunVO) {
        try {
            if (!runtimeCatalogService.isJudgeLanguageSupported(codeRunVO.getLanguage())) {
                return ResultUtil.error(400, "Unsupported language: " + codeRunVO.getLanguage());
            }

            if (codeRunVO.getProblemId() != null
                    && !problemService.isLanguageEnabledForProblem(codeRunVO.getProblemId(), codeRunVO.getLanguage())) {
                return ResultUtil.error(400, "Language is not enabled for this problem");
            }

            CodeExecutionResult result = codeSandboxService.executeCode(
                    codeRunVO.getCode(),
                    codeRunVO.getLanguage(),
                    codeRunVO.getInput()
            );
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error("运行失败：" + e.getMessage());
        }
    }

}
