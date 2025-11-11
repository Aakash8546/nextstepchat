package com.nextstep.chat.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public enum MessageType {
    TEXT,
    IMAGE,
    PDF,
    DOCUMENT
}
