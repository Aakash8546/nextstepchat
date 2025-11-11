package com.nextstep.chat.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

public enum UserType {
    JOB_SEEKER,
    JOB_GIVER
}
