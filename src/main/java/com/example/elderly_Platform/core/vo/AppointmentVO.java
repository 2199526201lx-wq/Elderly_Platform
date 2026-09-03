package com.example.elderly_Platform.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Long id;
    private Long userId;
    private Long slotId;
    private Long packageId;
    private String status;
    private String reportUrl;
    private LocalDateTime createTime;
}
