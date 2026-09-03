package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiConversationMessage {
    private Long id;
    private Long sessionId;
    private Long userId;
    private String role;
    private String message;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
