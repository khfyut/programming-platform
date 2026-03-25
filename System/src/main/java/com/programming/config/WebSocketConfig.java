package com.programming.config;

import com.programming.handler.JudgeProgressHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(judgeProgressHandler(), "/ws/judge-progress")
                .setAllowedOrigins("*");
    }
    
    @Bean
    public JudgeProgressHandler judgeProgressHandler() {
        return new JudgeProgressHandler();
    }
}
