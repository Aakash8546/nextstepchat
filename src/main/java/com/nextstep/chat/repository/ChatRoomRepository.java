package com.nextstep.chat.repository;

import com.nextstep.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByChatRoomId(String chatRoomId);

    @Query("SELECT c FROM ChatRoom c WHERE (c.jobSeekerId = ?1 AND c.jobGiverId = ?2) OR (c.jobSeekerId = ?2 AND c.jobGiverId = ?1)")
    Optional<ChatRoom> findByUserIds(Long userId1, Long userId2);

    @Query("SELECT c FROM ChatRoom c WHERE c.jobSeekerId = ?1 OR c.jobGiverId = ?1 ORDER BY c.lastMessageAt DESC")
    List<ChatRoom> findByUserId(Long userId);
}
