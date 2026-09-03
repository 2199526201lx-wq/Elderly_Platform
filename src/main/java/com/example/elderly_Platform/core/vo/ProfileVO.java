package com.example.elderly_Platform.core.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProfileVO {
    private Long id;
    private String phone;
    private String realName;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal height;
    private String avatar;
    private String emergencyContact;
    private String memberLevel;
    private Integer points;
    private String status;
    private LocalDateTime createTime;
}
