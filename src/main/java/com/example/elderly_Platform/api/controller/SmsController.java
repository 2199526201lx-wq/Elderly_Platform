package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.SmsService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.SendCodeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信验证码控制器 —— 负责发送短信验证码
 * <p>该模块无需登录即可访问（已在 SecurityConfig 中配置放行 /api/sms/**），
 * 用于注册、重置密码等场景的身份验证</p>
 */
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {
    private final SmsService smsService;

    /**
     * 发送短信验证码
     * <p>流程：生成6位随机验证码 → 保存到数据库（含过期时间）→ 调用短信服务发送</p>
     * <p>注意：验证码有效期默认 5 分钟，且同一手机号在发送间隔内会提示请勿频繁发送</p>
     *
     * @param dto 发送信息：phone（手机号，需为合法大陆手机号）
     * @return 发送成功后返回空数据
     */
    @PostMapping("/send")
    public Result<?> send(@Valid @RequestBody SendCodeDTO dto){
        smsService.sendCode(dto.getPhone());
        return Result.success();
    }
}
