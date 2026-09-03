package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityRegistration {
    private Long id;
    private Long userId;
    private Long activityId;
    private String checkInStatus;
    private LocalDateTime checkInTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
