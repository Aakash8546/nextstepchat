package com.nextstep.chat.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatRoomId;
    private Long senderId;
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String fileUrl;
    private String fileName;
    private Long fileSize;

    private boolean isPinned;
    private LocalDateTime pinnedAt;

    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime sentAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageReaction> reactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        isRead = false;
        isPinned = false;
    }
}
