package com.nextstep.chat.dto;

import com.nextstep.chat.model.MessageType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class ReactionDTO {
    private Long id;
    private Long userId;
    private String emoji;
    private LocalDateTime createdAt;
}
