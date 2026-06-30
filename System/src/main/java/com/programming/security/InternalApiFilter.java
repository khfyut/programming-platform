package com.programming.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 内部 API 认证过滤器。
 *
 * Python Agent 通过 X-Internal-Key header 调 Java API 时，
 * 此过滤器检测到匹配的密钥后，直接设置系统级认证上下文，
 * 跳过 JWT 校验。
 *
 * 注册顺序：必须在 JwtAuthenticationFilter 之前执行。
 */
@Component
public class InternalApiFilter extends OncePerRequestFilter {

    // 从 application.yml 读取共享密钥
    @Value("${programming.internal-api-key:phoebe-shared-secret-2026}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 读取 X-Internal-Key header
        String headerKey = request.getHeader("X-Internal-Key");

        // 密钥匹配 → 设置系统认证，跳过 JWT
        if (headerKey != null && headerKey.equals(internalApiKey)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            0L,  // system user ID
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("userId", 0L);
            request.setAttribute("isAdmin", true);
        }

        filterChain.doFilter(request, response);
    }
}