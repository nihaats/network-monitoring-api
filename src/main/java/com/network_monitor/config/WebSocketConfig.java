package com.network_monitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple broker for sending messages to clients
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint - matches Angular WS_ENDPOINT
        registry.addEndpoint("/ws/snmp-data")
                .setAllowedOriginPatterns("*") // CORS için
                .withSockJS(); // SockJS fallback support

        // Raw WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws/snmp-data")
                .setAllowedOriginPatterns("*");
    }
}