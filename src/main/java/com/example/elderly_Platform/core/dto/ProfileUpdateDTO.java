package com.example.elderly_Platform.core.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProfileUpdateDTO {
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    private String gender;

    private LocalDate birthDate;

    @DecimalMin(value = "30.0", message = "身高不能小于30cm")
    @DecimalMax(value = "250.0", message = "身高不能大于250cm")
    private BigDecimal height;

    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    @Size(max = 20, message = "紧急联系人电话长度不能超过20个字符")
    private String emergencyContact;
}
