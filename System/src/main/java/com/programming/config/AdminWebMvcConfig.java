package com.programming.config;

import com.programming.interceptor.AdminInterceptor;
import com.programming.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/problem/list",
                        "/api/problem/detail/*",
                        "/api/problem/*/test-cases/sample",
                        "/api/problem/tag/*",
                        "/api/problem/languages",
                        "/api/problem/categories",
                        "/api/community/posts",
                        "/api/community/post/*",
                        "/api/community/post/*/comments",
                        "/api/community/statistics",
                        "/api/community/hot-topics"
                );

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(
                        "/api/admin/**",
                        "/api/problem/test-jdbc",
                        "/api/problem/*/test-cases/all",
                        "/api/problem/*/test-cases",
                        "/api/problem/*/test-cases/batch",
                        "/api/problem/test-cases/*"
                );
    }
}
