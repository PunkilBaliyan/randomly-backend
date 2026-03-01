package com.randomly.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for STOMP message broker.
 * Enables real-time bidirectional communication.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker for pub/sub messaging
        // Note: Heartbeat is handled by WebSocket transport layer, not needed here
        registry.enableSimpleBroker("/topic", "/queue");
        
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:4200",     // Local dev
                        "http://localhost:3000",     // Alt dev port
                        "https://randomly-frontend.onrender.com", // Production frontend
                        "https://randomly-frontend-one.vercel.app"    // Alternative prod frontend
                )
                .withSockJS()
                .setInterceptors(new WebSocketInterceptor());
    }
}


