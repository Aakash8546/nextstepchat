package com.nextstep.chat.service;

import com.nextstep.chat.dto.MessageDTO;
import com.nextstep.chat.dto.ReactionDTO;
import com.nextstep.chat.model.*;
import com.nextstep.chat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageReactionRepository reactionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageDTO sendTextMessage(Long chatRoomId, Long senderId, Long receiverId, String content) {
        Message message = new Message();
        message.setChatRoomId(chatRoomId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageType(MessageType.TEXT);
        message.setContent(content);

        Message savedMessage = messageRepository.save(message);
        updateChatRoomLastMessage(chatRoomId);

        MessageDTO dto = convertToDTO(savedMessage);

        // Send via WebSocket
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                dto
        );

        return dto;
    }

    @Transactional
    public MessageDTO sendFileMessage(Long chatRoomId, Long senderId, Long receiverId,
                                      MultipartFile file, MessageType messageType) throws IOException {
        String fileName = fileStorageService.storeFile(file);

        Message message = new Message();
        message.setChatRoomId(chatRoomId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageType(messageType);
        message.setFileName(file.getOriginalFilename());
        message.setFileUrl("/api/files/" + fileName);
        message.setFileSize(file.getSize());

        Message savedMessage = messageRepository.save(message);
        updateChatRoomLastMessage(chatRoomId);

        MessageDTO dto = convertToDTO(savedMessage);

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                dto
        );

        return dto;
    }

    public List<MessageDTO> getChatMessages(Long chatRoomId) {
        List<Message> messages = messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getPinnedMessages(Long chatRoomId) {
        List<Message> pinnedMessages = messageRepository.findByChatRoomIdAndIsPinnedTrueOrderByPinnedAtDesc(chatRoomId);
        return pinnedMessages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDTO pinMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setPinned(true);
        message.setPinnedAt(LocalDateTime.now());
        Message updated = messageRepository.save(message);

        MessageDTO dto = convertToDTO(updated);

        // Notify both users
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatRoomId() + "/pin",
                dto
        );

        return dto;
    }

    @Transactional
    public MessageDTO unpinMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setPinned(false);
        message.setPinnedAt(null);
        Message updated = messageRepository.save(message);

        MessageDTO dto = convertToDTO(updated);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatRoomId() + "/unpin",
                dto
        );

        return dto;
    }

    @Transactional
    public MessageDTO addReaction(Long messageId, Long userId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Check if user already reacted with this emoji
        var existingReaction = reactionRepository.findByMessageIdAndUserIdAndEmoji(messageId, userId, emoji);

        if (existingReaction.isPresent()) {
            // Remove reaction if already exists
            reactionRepository.delete(existingReaction.get());
        } else {
            // Add new reaction
            MessageReaction reaction = new MessageReaction();
            reaction.setMessage(message);
            reaction.setUserId(userId);
            reaction.setEmoji(emoji);
            reactionRepository.save(reaction);
        }

        // Reload message with reactions
        message = messageRepository.findById(messageId).get();
        MessageDTO dto = convertToDTO(message);

        // Notify via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatRoomId() + "/reaction",
                dto
        );

        return dto;
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getReceiverId().equals(userId) && !message.isRead()) {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);

            // Notify sender via WebSocket
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId().toString(),
                    "/queue/read-receipt",
                    messageId
            );
        }
    }

    private void updateChatRoomLastMessage(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        chatRoom.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoomId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setMessageType(message.getMessageType());
        dto.setContent(message.getContent());
        dto.setFileUrl(message.getFileUrl());
        dto.setFileName(message.getFileName());
        dto.setFileSize(message.getFileSize());
        dto.setPinned(message.isPinned());
        dto.setPinnedAt(message.getPinnedAt());
        dto.setRead(message.isRead());
        dto.setSentAt(message.getSentAt());

        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        if (sender != null) {
            dto.setSenderName(sender.getUsername());
        }

        List<ReactionDTO> reactions = message.getReactions().stream()
                .map(r -> {
                    ReactionDTO rdto = new ReactionDTO();
                    rdto.setId(r.getId());
                    rdto.setUserId(r.getUserId());
                    rdto.setEmoji(r.getEmoji());
                    rdto.setCreatedAt(r.getCreatedAt());
                    return rdto;
                })
                .collect(Collectors.toList());
        dto.setReactions(reactions);

        return dto;
    }
}