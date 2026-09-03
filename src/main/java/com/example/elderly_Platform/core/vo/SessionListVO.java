package com.example.elderly_Platform.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionListVO {
    private Long sessionID;
    private String sessionName;
    private LocalDateTime updateTime;
}
