package com.programming.interceptor;

import com.programming.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        log.info("JWT拦截器 - 请求路径: {}, Authorization: {}", request.getRequestURI(), token);
        
        if (token == null || token.isEmpty()) {
            log.warn("JWT拦截器 - token为空");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或token已过期\",\"data\":null}");
            return false;
        }
        
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info("JWT拦截器 - 移除Bearer前缀后的token: {}", token);
        }
        
        if (!jwtUtil.validateToken(token)) {
            log.error("JWT拦截器 - token验证失败");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token无效\",\"data\":null}");
            return false;
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("JWT拦截器 - token验证成功，用户ID: {}", userId);
        request.setAttribute("userId", userId);
        
        return true;
    }
}