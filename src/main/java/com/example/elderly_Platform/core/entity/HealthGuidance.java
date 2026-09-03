package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthGuidance {
    private Long id;
    private Long userId;
    private String type;
    private String indicator;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
