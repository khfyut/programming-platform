package com.programming.service.impl;

import com.programming.entity.User;
import com.programming.mapper.UserMapper;
import com.programming.service.UserService;
import com.programming.util.JwtUtil;
import com.programming.util.PasswordUtil;
import com.programming.vo.ChangePasswordVO;
import com.programming.vo.LoginVO;
import com.programming.vo.RegisterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    private boolean isEncodedPassword(String password) {
        return password != null
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (isEncodedPassword(storedPassword)) {
            return passwordUtil.matches(rawPassword, storedPassword);
        }

        return rawPassword.equals(storedPassword);
    }

    @Override
    public Map<String, Object> login(LoginVO loginVO) {
        User user = userMapper.findByUsername(loginVO.getUsername());

        if (user == null || !matchesPassword(loginVO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // Smoothly migrate legacy plain-text passwords after a successful login.
        if (!isEncodedPassword(user.getPassword())) {
            userMapper.updatePassword(user.getId(), passwordUtil.encode(loginVO.getPassword()));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return result;
    }

    @Override
    public void register(RegisterVO registerVO) {
        User existingUser = userMapper.findByUsername(registerVO.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerVO.getUsername());
        user.setPassword(passwordUtil.encode(registerVO.getPassword()));
        user.setRole(0);
        user.setLanguage("java");
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public void updateLanguage(Long userId, String language) {
        userMapper.updateLanguage(userId, language);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordVO changePasswordVO) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (changePasswordVO == null
                || changePasswordVO.getOldPassword() == null
                || changePasswordVO.getOldPassword().isBlank()
                || changePasswordVO.getNewPassword() == null
                || changePasswordVO.getNewPassword().isBlank()) {
            throw new RuntimeException("密码不能为空");
        }

        if (!matchesPassword(changePasswordVO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }

        if (changePasswordVO.getOldPassword().equals(changePasswordVO.getNewPassword())) {
            throw new RuntimeException("新密码不能与当前密码相同");
        }

        userMapper.updatePassword(userId, passwordUtil.encode(changePasswordVO.getNewPassword()));
    }
}
