package com.example.elderly_Platform.core.entity;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String phone;
    private String password;
    private String realName;
    private String gender;
    private LocalDateTime birthDate;
    private BigDecimal height;
    private String avatar;
    private String emergencyContact;
    private String memberLevel;
    private Integer points;
    private String status;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
