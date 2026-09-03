package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefreshToken {
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
