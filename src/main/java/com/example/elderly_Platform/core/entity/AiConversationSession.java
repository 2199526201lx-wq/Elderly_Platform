package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiConversationSession {
    private Long id;
    private Long userId;
    private String sessionName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
