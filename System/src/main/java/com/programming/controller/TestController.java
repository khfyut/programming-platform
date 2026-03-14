package com.programming.controller;

import com.programming.util.PasswordUtil;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private PasswordUtil passwordUtil;

    @GetMapping("/generate-password")
    public ResultUtil<Map<String, String>> generatePassword() {
        String password = "admin123";
        String hashedPassword = passwordUtil.encode(password);
        
        Map<String, String> result = new HashMap<>();
        result.put("originalPassword", password);
        result.put("hashedPassword", hashedPassword);
        result.put("sql", "UPDATE user SET password = '" + hashedPassword + "' WHERE username = 'admin';");
        
        return ResultUtil.success(result);
    }
}