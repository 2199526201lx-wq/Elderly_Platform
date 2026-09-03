package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.AuthService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.*;
import com.example.elderly_Platform.core.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 —— 处理用户注册、登录、Token 刷新、修改密码、退出登录、重置密码等认证相关操作
 * <p>
 * 该模块的所有接口均无需登录即可访问（已在 SecurityConfig 中配置放行 /api/auth/**）
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册
     * <p>流程：验证短信验证码 → 检查手机号是否已注册 → 创建用户（初始积分100）→ 记录注册赠送积分流水</p>
     *
     * @param dto 注册信息：手机号 phone、短信验证码 code、密码 password
     * @return 注册成功后返回空数据；验证码错误或手机号已注册则抛出异常
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    /**
     * 用户登录
     * <p>流程：校验手机号和密码 → 检查账号状态（禁用账号不可登录）→ 生成 accessToken 和 refreshToken → 保存 refreshToken 到数据库</p>
     *
     * @param dto 登录信息：手机号 phone、密码 password
     * @return 登录成功后返回 {@link LoginVO}，包含 accessToken（2小时有效）、refreshToken（7天有效）和用户信息
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }

    /**
     * 刷新访问令牌
     * <p>当 accessToken 过期时，使用 refreshToken 换取新的令牌对</p>
     *
     * @param dto 刷新信息：refreshToken（刷新令牌）
     * @return 新的 {@link LoginVO}，包含新的 accessToken 和 refreshToken
     */
    @PostMapping("/refresh")
    public Result<?> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        LoginVO vo = authService.refreshToken(dto);
        return Result.success(vo);
    }

    /**
     * 修改密码
     * <p>需登录后调用，从安全上下文中获取当前用户 ID</p>
     * <p>修改密码后，旧 Token 全部失效（通过 pw_changed 时间戳机制），需重新登录</p>
     *
     * @param dto 修改密码信息：oldPassword（原密码）、newPassword（新密码）
     * @return 修改成功后返回空数据
     */
    @PostMapping("/change-password")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.changePassword(userId, dto);
        return Result.success();
    }

    /**
     * 退出登录
     * <p>流程：将当前 accessToken 加入 Redis 黑名单（2小时）→ 删除数据库中的 refreshToken</p>
     *
     * @param dto 退出信息：accessToken（要拉黑的访问令牌）、refreshToken（要删除的刷新令牌）
     * @return 退出成功后返回空数据
     */
    @PostMapping("/logout")
    public Result<?> logout(@Valid @RequestBody LogoutDTO dto) {
        authService.logout(dto);
        return Result.success();
    }

    /**
     * 重置密码（忘记密码）
     * <p>无需登录，通过短信验证码验证身份后重置密码</p>
     * <p>重置密码后，旧 Token 全部失效，需重新登录</p>
     *
     * @param dto 重置信息：手机号 phone、短信验证码 code、新密码 newPassword
     * @return 重置成功后返回空数据
     */
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.success();
    }
}
