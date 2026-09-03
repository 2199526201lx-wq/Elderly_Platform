package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthRecord {
    private Long id;
    private Long userId;
    private Integer systolic;
    private Integer diastolic;
    private BigDecimal bloodSugar;
    private Integer heartRate;
    private BigDecimal weight;
    private BigDecimal bmi;
    private String memo;
    private LocalDateTime recordedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
