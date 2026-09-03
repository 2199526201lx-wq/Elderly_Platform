package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private Integer isRead;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
