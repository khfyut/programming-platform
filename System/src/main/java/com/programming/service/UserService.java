package com.programming.service;

import com.programming.entity.User;
import com.programming.vo.LoginVO;
import com.programming.vo.RegisterVO;

import java.util.Map;

public interface UserService {
    Map<String, Object> login(LoginVO loginVO);
    void register(RegisterVO registerVO);
    User getUserInfo(Long userId);
    void updateLanguage(Long userId, String language);
}
