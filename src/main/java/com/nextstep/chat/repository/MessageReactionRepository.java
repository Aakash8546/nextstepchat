package com.nextstep.chat.repository;

import com.nextstep.chat.model.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {
    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);
}