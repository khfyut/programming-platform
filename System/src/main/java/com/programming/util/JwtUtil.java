package com.programming.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${programming.jwt.secret}")
    private String secret;

    @Value("${programming.jwt.expire}")
    private long expire;

    public String generateToken(Long userId, Integer role) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        String token = Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(key)
                .compact();
        log.info("JWT生成 - 用户ID: {}, 角色: {}, Token: {}", userId, role, token);
        return token;
    }

    public Long getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Long userId = Long.parseLong(claims.getSubject());
            log.info("JWT解析 - Token: {}, 用户ID: {}", token, userId);
            return userId;
        } catch (Exception e) {
            log.error("JWT解析失败 - Token: {}, 错误: {}", token, e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.info("JWT验证成功 - Token: {}", token);
            return true;
        } catch (Exception e) {
            log.error("JWT验证失败 - Token: {}, 错误: {}", token, e.getMessage());
            return false;
        }
    }

    public Integer getRoleFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object role = claims.get("role");
            if (role != null) {
                return Integer.parseInt(role.toString());
            }
            return 0;
        } catch (Exception e) {
            log.error("JWT解析role失败 - Token: {}, 错误: {}", token, e.getMessage());
            return null;
        }
    }
}
