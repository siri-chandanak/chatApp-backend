package com.chatApp.backend.security;

import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WsAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WsAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                if (jwtService.isValid(token)) {
                    Long userId = jwtService.getUserId(token);
                    accessor.getSessionAttributes().put("userId", userId);
                }
            }
        }
        return message;
    }
}