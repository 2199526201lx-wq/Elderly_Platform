package com.example.elderly_Platform.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message="手机号不能为空")
    private String phone;
    @NotBlank(message="密码不能为空")
    private String password;
    /** 登录身份：MEMBER（会员端）或 ADMIN（管理端） */
    private String role;
}
