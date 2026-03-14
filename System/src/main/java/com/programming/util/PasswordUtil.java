package com.programming.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        String encoded = encoder.encode(rawPassword);
        System.out.println("BCrypt编码: " + rawPassword + " -> " + encoded);
        return encoded;
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        System.out.println("BCrypt验证: " + rawPassword + " vs " + encodedPassword);
        boolean result = encoder.matches(rawPassword, encodedPassword);
        System.out.println("BCrypt验证结果: " + result);
        return result;
    }
}