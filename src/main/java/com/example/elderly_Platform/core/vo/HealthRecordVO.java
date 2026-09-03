package com.example.elderly_Platform.core.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthRecordVO {
    // 收缩压统计
    private BigDecimal systolicAvg;
    private BigDecimal systolicMax;
    private BigDecimal systolicMin;

    // 舒张压统计
    private BigDecimal diastolicAvg;
    private BigDecimal diastolicMax;
    private BigDecimal diastolicMin;

    // 空腹血糖统计
    private BigDecimal bloodSugarAvg;
    private BigDecimal bloodSugarMax;
    private BigDecimal bloodSugarMin;

    // 心率统计
    private BigDecimal heartRateAvg;
    private BigDecimal heartRateMax;
    private BigDecimal heartRateMin;

    // BMI 统计
    private BigDecimal bmiAvg;
    private BigDecimal bmiMax;
    private BigDecimal bmiMin;

}
