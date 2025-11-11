package com.nextstep.chat.dto;

import com.nextstep.chat.model.MessageType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageDTO {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private MessageType messageType;
    private String content;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private boolean Pinned;
    private LocalDateTime pinnedAt;
    private boolean Read;
    private LocalDateTime sentAt;
    private List<ReactionDTO> reactions;
}

