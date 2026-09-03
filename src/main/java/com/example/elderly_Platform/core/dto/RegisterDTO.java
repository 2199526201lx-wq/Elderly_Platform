package com.example.elderly_Platform.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message="手机号不可为空")
    private String phone;

    @NotBlank(message="密码不能为空")
    private String password;

    @NotBlank(message="验证码不能为空")
    private String code;
}
