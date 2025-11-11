package com.nextstep.chat.service;

import com.nextstep.chat.dto.ChatRoomDTO;
import com.nextstep.chat.dto.MessageDTO;
import com.nextstep.chat.model.*;
import com.nextstep.chat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoom createOrGetChatRoom(Long userId1, Long userId2) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUserIds(userId1, userId2);

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(UUID.randomUUID().toString());

        if (user1.getUserType() == UserType.JOB_SEEKER) {
            chatRoom.setJobSeekerId(userId1);
            chatRoom.setJobGiverId(userId2);
        } else {
            chatRoom.setJobSeekerId(userId2);
            chatRoom.setJobGiverId(userId1);
        }

        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoomDTO> getUserChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserId(userId);

        return chatRooms.stream().map(room -> {
            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setId(room.getId());
            dto.setChatRoomId(room.getChatRoomId());
            dto.setLastMessageAt(room.getLastMessageAt());

            Long otherUserId = room.getJobSeekerId().equals(userId) ?
                    room.getJobGiverId() : room.getJobSeekerId();

            User otherUser = userRepository.findById(otherUserId).orElse(null);
            if (otherUser != null) {
                dto.setOtherUserId(otherUser.getId());
                dto.setOtherUserName(otherUser.getUsername());
                dto.setOtherUserProfilePic(otherUser.getProfilePicture());
                dto.setOtherUserOnline(otherUser.isOnline());
                dto.setOtherUserLastSeen(otherUser.getLastSeen());
            }

            Message lastMessage = messageRepository.findLastMessageByChatRoomId(room.getId());
            if (lastMessage != null) {
                dto.setLastMessage(convertToDTO(lastMessage));
            }

            int unreadCount = messageRepository.countUnreadMessages(room.getId(), userId);
            dto.setUnreadCount(unreadCount);

            return dto;
        }).collect(Collectors.toList());
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
        return dto;
    }
}