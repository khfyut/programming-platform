package com.programming.controller;

import com.programming.service.CodeSandboxService;
import com.programming.util.ResultUtil;
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

    @PostMapping("/run")
    public ResultUtil<String> runCode(@RequestBody CodeRunVO codeRunVO) {
        try {
            String result = codeSandboxService.runCode(
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
