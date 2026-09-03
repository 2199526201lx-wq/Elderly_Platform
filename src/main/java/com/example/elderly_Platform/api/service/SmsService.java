package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.entity.SmsCode;
import com.example.elderly_Platform.core.mapper.SmsCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 短信验证码服务 —— 处理短信验证码的生成、存储与模拟发送
 */
@Service
@RequiredArgsConstructor
public class SmsService {
    private final SmsCodeMapper smsCodeMapper;

    /**
     * 发送短信验证码
     * <p>当前为随机生成验证码并模拟发送，后续可对接阿里云短信服务</p>
     *
     * @param phone 手机号
     */
    public void sendCode(String phone){
        //当前随机生成，后期阿里云
        String code =String.format("%06d",new Random().nextInt(100000));

        SmsCode sc = new SmsCode();
        sc.setPhone(phone);
        sc.setCode(code);
        sc.setExpireTime(LocalDateTime.now().plusMinutes(5));
        sc.setCreateTime(LocalDateTime.now());
        smsCodeMapper.insert(sc);

        //模拟发送
        System.out.println("shoujihao:"+phone+"yanzhengma:"+code);
    }
}
