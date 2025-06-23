package com.swp2.demo.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Messages sent from the client with a destination starting with /app will be routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
        // Use a simple in-memory message broker to carry messages back to the client on destinations prefixed with /user
        registry.enableSimpleBroker("/user");
        // This prefix will be used for personal, one-to-one messages
        registry.setUserDestinationPrefix("/user");
    }
}
