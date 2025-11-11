package com.nextstep.chat.repository;

import com.nextstep.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    List<Message> findByChatRoomIdAndIsPinnedTrueOrderByPinnedAtDesc(Long chatRoomId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoomId = ?1 AND m.receiverId = ?2 AND m.isRead = false")
    int countUnreadMessages(Long chatRoomId, Long userId);

    @Query("SELECT m FROM Message m WHERE m.chatRoomId = ?1 ORDER BY m.sentAt DESC LIMIT 1")
    Message findLastMessageByChatRoomId(Long chatRoomId);
}