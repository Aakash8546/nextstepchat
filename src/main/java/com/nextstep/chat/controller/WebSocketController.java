package com.nextstep.chat.controller;

import com.nextstep.chat.dto.MessageDTO;
import com.nextstep.chat.service.MessageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat/{chatRoomId}/send")
    public void sendMessage(@DestinationVariable Long chatRoomId, @Payload MessageDTO messageDTO) {
        MessageDTO savedMessage = messageService.sendTextMessage(
                chatRoomId,
                messageDTO.getSenderId(),
                messageDTO.getReceiverId(),
                messageDTO.getContent()
        );

        // Broadcast to chat room
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, savedMessage);
    }

    @MessageMapping("/chat/{chatRoomId}/typing")
    public void typing(@DestinationVariable Long chatRoomId, @Payload TypingNotification notification) {
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/typing", notification);
    }

    @MessageMapping("/user/status")
    public void updateUserStatus(@Payload UserStatus status) {
        messagingTemplate.convertAndSend("/topic/user/" + status.getUserId() + "/status", status);
    }

    static class TypingNotification {
        private Long userId;
        private String username;
        private boolean typing;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public boolean isTyping() { return typing; }
        public void setTyping(boolean typing) { this.typing = typing; }
    }

    @Setter
    @Getter
    static class UserStatus {
        private Long userId;
        private boolean online;

    }
}