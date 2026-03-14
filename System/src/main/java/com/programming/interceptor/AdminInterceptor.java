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
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        log.info("管理员拦截器 - 请求路径: {}, Authorization: {}", request.getRequestURI(), token);
        
        if (token == null || token.isEmpty()) {
            log.warn("管理员拦截器 - token为空");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\",\"data\":null}");
            return false;
        }
        
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info("管理员拦截器 - 移除Bearer前缀后的token: {}", token);
        }
        
        if (!jwtUtil.validateToken(token)) {
            log.error("管理员拦截器 - token验证失败");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token无效\",\"data\":null}");
            return false;
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        
        if (role == null || role != 1) {
            log.warn("管理员拦截器 - 用户ID: {}, 角色: {}, 不是管理员", userId, role);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"无权限访问\",\"data\":null}");
            return false;
        }
        
        log.info("管理员拦截器 - 验证成功，管理员ID: {}", userId);
        request.setAttribute("userId", userId);
        request.setAttribute("isAdmin", true);
        
        return true;
    }
}