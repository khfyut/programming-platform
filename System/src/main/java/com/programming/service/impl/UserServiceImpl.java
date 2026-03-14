package com.programming.service.impl;

import com.programming.entity.User;
import com.programming.mapper.UserMapper;
import com.programming.service.UserService;
import com.programming.util.JwtUtil;
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

    @Override
    public Map<String, Object> login(LoginVO loginVO) {
        User user = userMapper.findByUsername(loginVO.getUsername());
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!loginVO.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
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
        user.setPassword(registerVO.getPassword());
        user.setRole(0);
        user.setLanguage("java");
        
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
}