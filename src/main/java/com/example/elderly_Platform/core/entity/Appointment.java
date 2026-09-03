package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Appointment {
    private Long id;
    private Long userId;
    private Long slotId;
    private Long packageId;
    private String status;
    private String reportUrl;
    private String originalFilename;
    private LocalDateTime reportUploadTime;
    private Long uploadAdminId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
