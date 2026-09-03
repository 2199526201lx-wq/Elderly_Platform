package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentPackage {
    private Long id;
    private String name;
    private String coverUrl;
    private String description;
    private Integer price;
    private String suitablePeople;
    private String items;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
