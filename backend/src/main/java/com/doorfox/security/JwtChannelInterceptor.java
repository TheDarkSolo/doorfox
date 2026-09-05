package com.doorfox.security;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * The /ws HTTP handshake itself is permitAll (SockJS info/polling requests need
 * to succeed before a STOMP session exists), so auth for the inbox WebSocket is
 * enforced here instead: the STOMP CONNECT frame must carry a valid JWT, or the
 * connection is rejected before any conversation topic can be subscribed to.
 */
@Slf4j
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public JwtChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing Authorization header on STOMP CONNECT");
            }
            try {
                jwtService.validateAndGetManagerId(authHeader.substring(7));
            } catch (JwtException | IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid JWT on STOMP CONNECT", e);
            }
        }
        return message;
    }
}
