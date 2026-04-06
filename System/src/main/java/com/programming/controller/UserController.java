package com.programming.controller;

import com.programming.entity.User;
import com.programming.service.UserService;
import com.programming.util.ResultUtil;
import com.programming.vo.ChangePasswordVO;
import com.programming.vo.LoginVO;
import com.programming.vo.RegisterVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResultUtil<Void> register(@RequestBody RegisterVO registerVO) {
        try {
            userService.register(registerVO);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResultUtil<Map<String, Object>> login(@RequestBody LoginVO loginVO) {
        try {
            Map<String, Object> result = userService.login(loginVO);
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public ResultUtil<User> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            User user = userService.getUserInfo(userId);
            return ResultUtil.success(user);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/update/language")
    public ResultUtil<Void> updateLanguage(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String language = params.get("language");
            userService.updateLanguage(userId, language);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping({"/password", "/change-password"})
    public ResultUtil<Void> changePassword(@RequestBody ChangePasswordVO changePasswordVO, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userService.changePassword(userId, changePasswordVO);
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
