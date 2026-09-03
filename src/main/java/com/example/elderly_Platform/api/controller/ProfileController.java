package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.ProfileService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.ProfileUpdateDTO;
import com.example.elderly_Platform.core.vo.ProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 个人信息控制器 —— 处理会员个人资料的查看与修改
 * <p>需会员登录后访问（需在请求头携带会员 Token）</p>
 */
@RestController
@RequestMapping("/api/member/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    /**
     * 获取当前登录用户的个人信息
     */
    @GetMapping
    public Result<?> getProfile() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ProfileVO vo = profileService.getProfile(userId);
        return Result.success(vo);
    }

    /**
     * 修改当前登录用户的个人信息
     */
    @PutMapping
    public Result<?> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        profileService.updateProfile(userId, dto);
        return Result.success();
    }
}
