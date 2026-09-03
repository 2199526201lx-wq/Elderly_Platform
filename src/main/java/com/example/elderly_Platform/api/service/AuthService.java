package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.dto.*;
import com.example.elderly_Platform.core.entity.PointTransaction;
import com.example.elderly_Platform.core.entity.RefreshToken;
import com.example.elderly_Platform.core.entity.SmsCode;
import com.example.elderly_Platform.core.entity.User;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.PointTransactionMapper;
import com.example.elderly_Platform.core.mapper.RefreshTokenMapper;
import com.example.elderly_Platform.core.mapper.SmsCodeMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.example.elderly_Platform.core.util.JwtUtil;
import com.example.elderly_Platform.core.util.RedisUtil;
import com.example.elderly_Platform.core.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 认证服务 —— 处理用户注册、登录、Token 刷新、修改密码、退出登录、重置密码等认证业务逻辑
 * <p>密码均通过 BCrypt 加密存储；登录成功后签发 accessToken（2小时）和 refreshToken（7天）</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final SmsCodeMapper smsCodeMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenMapper refreshTokenMapper;
    private final RedisUtil redisUtil;

    /**
     * 用户注册
     * <p>流程：校验短信验证码 → 检查手机号是否已注册 → 创建用户（默认等级"普通"、初始积分100）→ 记录注册赠送积分流水</p>
     *
     * @param dto 注册信息：手机号、短信验证码、密码
     * @throws BusinessException 验证码错误/过期、手机号已被注册时抛出
     */
    @Transactional
    public void register(RegisterDTO dto){
        SmsCode smsCode = smsCodeMapper.selectLatestByPhone(dto.getPhone());

        if(smsCode == null || !smsCode.getCode().equals(dto.getCode())){
            throw new BusinessException("验证码错误");
        }
        if(smsCode.getExpireTime().isBefore(LocalDateTime.now())){
            throw new BusinessException("验证码已过期");
        }
        smsCodeMapper.markUsed(smsCode.getId());

        if(userMapper.selectByPhone(dto.getPhone()) != null){
            throw new BusinessException("手机号已被注册");
        }

        User user=new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setMemberLevel("普通");
        user.setPoints(100);
        user.setStatus("启用");
        user.setRole("MEMBER");
        userMapper.insert(user);

        PointTransaction ps=new PointTransaction();
        ps.setUserId(user.getId());
        ps.setType("注册赠送");
        ps.setChangeAmount(100);
        ps.setBalanceAfter(100);
        ps.setRemainAmount(100);
        ps.setExpireTime(LocalDateTime.now().plusYears(1));
        ps.setDescription("注册送积分");
        pointTransactionMapper.insert(ps);
    }

    /**
     * 用户登录
     * <p>流程：校验手机号和密码 → 检查账号是否被禁用 → 生成 accessToken/refreshToken → 保存 refreshToken 到数据库</p>
     *
     * @param dto 登录信息：手机号、密码
     * @return 包含 accessToken、refreshToken 和用户信息的 LoginVO
     * @throws BusinessException 手机号或密码错误、账号被禁用时抛出
     */
    public LoginVO login(LoginDTO dto){
        User user =userMapper.selectByPhone(dto.getPhone());
        if(user == null||!passwordEncoder.matches(dto.getPassword(),user.getPassword())){
            throw new BusinessException("手机号或密码错误");
        }
        if("禁用".equals(user.getStatus())){
            throw new BusinessException("账号已被禁用");
        }
        // 统一登录：前端不再区分登录入口，后端校验用户角色并由前端根据返回的 role 自行分流
        // 如果前端明确传了期望角色，做一次校验；否则不做限制（统一登录页不传 role）
        if (dto.getRole() != null && !dto.getRole().isEmpty()
                && !dto.getRole().equals(user.getRole())) {
            throw new BusinessException("该账号不是" + ("ADMIN".equals(dto.getRole()) ? "管理员" : "会员") + "，请到对应入口登录");
        }

        String accessToken=jwtUtil.generateAccessToken(user.getId(),user.getRole());
        String refreshToken=jwtUtil.generateRefreshToken(user.getId(),user.getRole());

        RefreshToken rt=new RefreshToken();
        rt.setUserId(user.getId());
        rt.setToken(refreshToken);
        rt.setExpireTime(LocalDateTime.now().plusSeconds(604800));
        rt.setCreateTime(LocalDateTime.now());
        refreshTokenMapper.insert(rt);

        LoginVO vo=new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setUserInfo(user);
        return vo;
    }

    /**
     * 刷新访问令牌
     * <p>校验 refreshToken 有效且未过期 → 生成新的令牌对 → 更新数据库中的 refreshToken</p>
     *
     * @param dto 刷新信息：refreshToken
     * @return 新的 accessToken 和 refreshToken
     * @throws BusinessException refreshToken 无效或已过期时抛出
     */
    public LoginVO refreshToken(RefreshTokenDTO dto){
        RefreshToken rt= refreshTokenMapper.selectByToken(dto.getRefreshToken());
        if(rt == null){
            throw new BusinessException("refreshToken无效，请重新登录");
        }
        if(rt.getExpireTime().isBefore(LocalDateTime.now())){
            throw new BusinessException("refreshToken已过期，请重新登录");
        }

        User user=userMapper.selectById(rt.getUserId());
        if(user == null){
            throw new BusinessException("用户不存在");
        }

        String newAccessToken=jwtUtil.generateAccessToken(user.getId(),user.getRole());
        String newRefreshToken=jwtUtil.generateRefreshToken(user.getId(),user.getRole());

        rt.setToken(newAccessToken);
        rt.setExpireTime(LocalDateTime.now().plusSeconds(604800));
        refreshTokenMapper.updateById(rt);

        LoginVO vo=new LoginVO();
        vo.setAccessToken(newAccessToken);
        vo.setRefreshToken(newRefreshToken);
        vo.setUserInfo(user);
        return vo;
    }

    /**
     * 修改密码
     * <p>校验原密码 → 更新为新密码 → 删除所有 refreshToken → 设置密码修改时间戳使旧 accessToken 失效</p>
     *
     * @param userId 当前登录用户 ID
     * @param dto    修改信息：原密码、新密码
     * @throws BusinessException 用户不存在或原密码错误时抛出
     */
    public void changePassword(Long userId,ChangePasswordDTO dto){
        User user=userMapper.selectById(userId);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        if(!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        refreshTokenMapper.deleteByUserId(userId);
        redisUtil.set("pw_changed:" + userId, String.valueOf(System.currentTimeMillis()), 604800);
    }

    /**
     * 退出登录
     * <p>将 accessToken 加入 Redis 黑名单（剩余有效期），并从数据库删除对应的 refreshToken</p>
     *
     * @param dto 退出信息：accessToken、refreshToken
     */
    public void logout(LogoutDTO dto){
        redisUtil.set("blacklist:access:" + dto.getAccessToken(), "1", 7200);
        refreshTokenMapper.deleteByToken(dto.getRefreshToken());
    }

    /**
     * 重置密码（忘记密码场景）
     * <p>流程：校验短信验证码 → 检查手机号是否注册 → 更新新密码 → 强制旧 Token 失效</p>
     *
     * @param dto 重置信息：手机号、短信验证码、新密码
     * @throws BusinessException 验证码错误/过期、手机号未注册时抛出
     */
    public void resetPassword(ResetPasswordDTO dto){
        SmsCode smsCode=smsCodeMapper.selectValid(dto.getPhone(),dto.getCode());
        if (smsCode == null) {
            throw new BusinessException("验证码错误或已过期");
        }
        smsCodeMapper.markUsed(smsCode.getId());
        // 2. 查用户
        User user = userMapper.selectByPhone(dto.getPhone());
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }
        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        // 4. 强制重新登录：删 refresh token + 旧 access token 失效
        refreshTokenMapper.deleteByUserId(user.getId());
        redisUtil.set("pw_changed:" + user.getId(), String.valueOf(System.currentTimeMillis()), 604800);
    }
}
