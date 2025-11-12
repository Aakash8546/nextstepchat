package com.nextstep.chat.listener;

import com.nextstep.chat.model.User;
import com.nextstep.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getFirstNativeHeader("userId");

        if (userId != null) {
            Long userIdLong = Long.parseLong(userId);
            userRepository.findById(userIdLong).ifPresent(user -> {
                user.setOnline(true);
                userRepository.save(user);

                // Broadcast user online status
                messagingTemplate.convertAndSend(
                        "/topic/user/" + userId + "/status",
                        new UserStatusMessage(userIdLong, true)
                );
            });

            log.info("User connected: " + userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getFirstNativeHeader("userId");

        if (userId != null) {
            Long userIdLong = Long.parseLong(userId);
            userRepository.findById(userIdLong).ifPresent(user -> {
                user.setOnline(false);
                user.setLastSeen(LocalDateTime.now());
                userRepository.save(user);


                messagingTemplate.convertAndSend(
                        "/topic/user/" + userId + "/status",
                        new UserStatusMessage(userIdLong, false)
                );
            });

            log.info("User disconnected: " + userId);
        }
    }

    static class UserStatusMessage {
        private Long userId;
        private boolean online;

        public UserStatusMessage(Long userId, boolean online) {
            this.userId = userId;
            this.online = online;
        }

        public Long getUserId() { return userId; }
        public boolean isOnline() { return online; }
    }
}