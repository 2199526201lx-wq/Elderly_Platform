package com.example.elderly_Platform.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthRecordDTO {
    @NotNull(message = "收缩压不能为空")
    private Integer systolic;

    @NotNull(message = "舒张压不能为空")
    private Integer diastolic;

    @NotNull(message = "血糖不能为空")
    private BigDecimal bloodSugar;

    @NotNull(message = "心率不能为空")
    private Integer heartRate;

    @NotNull(message = "体重不能为空")
    private BigDecimal weight;

    private LocalDateTime recordedTime;

}
