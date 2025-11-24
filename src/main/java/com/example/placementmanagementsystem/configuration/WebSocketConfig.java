package com.example.placementmanagementsystem.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration class, used by the Student-Tutor messaging feature
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origin:http://localhost:8080}") // Set to host in application.properties
    private String allowedOrigin;

    /**
     * Configure message broker options
     *
     * @param registry MessageBrokerRegistry object to configure message broker
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");  // Enable in-memory message broker
        registry.setApplicationDestinationPrefixes("/app");  // Prefix for application endpoints
    }

    /**
     * Register STOMP endpoints for WebSocket
     *
     * @param registry StompEndpointRegistry object to register STOMP endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // WebSocket endpoint
                .setAllowedOrigins(allowedOrigin)  // Allowed origin
                .withSockJS();  // Enable SockJS fallback options
    }
}
