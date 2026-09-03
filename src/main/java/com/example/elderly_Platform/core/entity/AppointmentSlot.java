package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentSlot {
    private Long id;
    private Long packageId;
    private LocalDate appointDate;
    private String timeRange;
    private Integer maxCount;
    private Integer currentCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
