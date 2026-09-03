package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsCode {
    private Long id;
    private String phone;
    private String code;
    private LocalDateTime expireTime;
    private Integer used;
    private LocalDateTime createTime;
}
