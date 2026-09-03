package com.example.elderly_Platform.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageListVO {
    private Long messageId;
    private String role;
    private String content;
    private LocalDateTime createTime;
}
