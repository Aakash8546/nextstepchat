package com.nextstep.chat.dto;

import com.nextstep.chat.model.UserType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private UserType userType;
    private String profilePicture;
    private boolean online;
    private LocalDateTime lastSeen;
    private LocalDateTime createdAt;

    // Additional fields for specific use cases
    private String status; // e.g., "Available", "Busy", "Away"
    private String bio;
    private String phoneNumber;
    private String location;
}