package com.blog.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@EnableWebSocket
@ConditionalOnWebApplication
public class WebSocketConfig {

    /**
     * 注册WebSocket端点
     * 注意：使用外部Servlet容器（如Tomcat）时不需要此Bean
     * 使用Spring Boot内嵌容器时需要此Bean
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
