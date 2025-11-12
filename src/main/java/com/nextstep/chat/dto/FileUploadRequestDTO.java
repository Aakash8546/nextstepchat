package com.nextstep.chat.dto;

import lombok.Data;

@Data
public class FileUploadRequestDTO {
    private Long chatRoomId;
    private Long senderId;
    private Long receiverId;
}
