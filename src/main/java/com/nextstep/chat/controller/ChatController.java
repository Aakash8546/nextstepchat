package com.nextstep.chat.controller;

import com.nextstep.chat.dto.ChatRoomDTO;
import com.nextstep.chat.dto.MessageDTO;
import com.nextstep.chat.model.ChatRoom;
import com.nextstep.chat.service.ChatRoomService;
import com.nextstep.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createChatRoom(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        ChatRoom chatRoom = chatRoomService.createOrGetChatRoom(userId1, userId2);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/rooms/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(@PathVariable Long userId) {
        List<ChatRoomDTO> chatRooms = chatRoomService.getUserChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<MessageDTO>> getChatMessages(@PathVariable Long chatRoomId) {
        List<MessageDTO> messages = messageService.getChatMessages(chatRoomId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/{chatRoomId}/pinned")
    public ResponseEntity<List<MessageDTO>> getPinnedMessages(@PathVariable Long chatRoomId) {
        List<MessageDTO> pinnedMessages = messageService.getPinnedMessages(chatRoomId);
        return ResponseEntity.ok(pinnedMessages);
    }

    @PostMapping("/message/text")
    public ResponseEntity<MessageDTO> sendTextMessage(
            @RequestParam Long chatRoomId,
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content) {
        MessageDTO message = messageService.sendTextMessage(chatRoomId, senderId, receiverId, content);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/message/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/message/{messageId}/pin")
    public ResponseEntity<MessageDTO> pinMessage(@PathVariable Long messageId) {
        MessageDTO message = messageService.pinMessage(messageId);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/message/{messageId}/unpin")
    public ResponseEntity<MessageDTO> unpinMessage(@PathVariable Long messageId) {
        MessageDTO message = messageService.unpinMessage(messageId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/message/{messageId}/reaction")
    public ResponseEntity<MessageDTO> addReaction(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam String emoji) {
        MessageDTO message = messageService.addReaction(messageId, userId, emoji);
        return ResponseEntity.ok(message);
    }
}
