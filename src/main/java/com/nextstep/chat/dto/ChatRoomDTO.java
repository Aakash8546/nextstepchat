package com.nextstep.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatRoomDTO {
    private Long id;
    private String chatRoomId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfilePic;
    private boolean otherUserOnline;
    private LocalDateTime otherUserLastSeen;
    private MessageDTO lastMessage;
    private int unreadCount;
    private LocalDateTime lastMessageAt;
}
