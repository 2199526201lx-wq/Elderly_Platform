package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.entity.PointTransaction;
import com.example.elderly_Platform.core.entity.User;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.PointTransactionMapper;
import com.example.elderly_Platform.core.mapper.RefreshTokenMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.example.elderly_Platform.core.util.RedisUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端会员服务 —— 处理会员列表查询、详情、状态管理、等级调整、积分调整、密码重置等业务
 */
@Service
@RequiredArgsConstructor
public class MemberService {
    private final UserMapper userMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // ==================== 会员列表 ====================
    /**
     * 分页查询会员列表（支持按手机号、姓名、状态筛选）
     *
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @param phone     手机号（可选，精确匹配）
     * @param realName  真实姓名（可选，模糊匹配）
     * @param status    会员状态（可选：启用/禁用）
     * @return 分页的会员列表
     */
    public PageInfo<User> listMembers(Integer pageNum, Integer pageSize, String phone, String realName, String status) {
        PageHelper.startPage(pageNum, pageSize);

        User condition = new User();
        condition.setPhone(phone);
        condition.setRealName(realName);
        condition.setStatus(status);
        condition.setRole("MEMBER");

        List<User> list = userMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    // ==================== 会员详情 ====================
    /**
     * 获取会员详情
     *
     * @param id 会员用户 ID
     * @return 会员信息
     * @throws BusinessException 会员不存在时抛出
     */
    public User getMemberDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("会员不存在");
        }
        return user;
    }

    // ==================== 启用/禁用 ====================
    /**
     * 启用/禁用会员账号
     * <p>禁用时：删除该用户的 refreshToken 并设置密码修改时间戳，使旧 Token 立即失效</p>
     *
     * @param id     会员用户 ID
     * @param status 目标状态：启用 或 禁用
     * @throws BusinessException 会员不存在或状态值无效时抛出
     */
    public void updateStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("会员不存在");
        }
        if (!"启用".equals(status) && !"禁用".equals(status)) {
            throw new BusinessException("状态值无效，只能是'启用'或'禁用'");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        if ("禁用".equals(status)) {
            // 禁用：删除 Refresh Token + 设置密码修改时间戳（使旧 Token 失效）
            refreshTokenMapper.deleteByUserId(id);
            redisUtil.set("pw_changed:" + id, String.valueOf(System.currentTimeMillis()), 604800);
        }
        // 启用时不需要额外操作：新登录的 Token 签发时间 > pw_changed 时间，自然通过
    }

    // ==================== 等级调整 ====================
    /**
     * 调整会员等级
     *
     * @param id    会员用户 ID
     * @param level 目标等级：普通/白银/黄金/铂金/钻石
     * @throws BusinessException 会员不存在或等级值无效时抛出
     */
    public void updateLevel(Long id, String level) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("会员不存在");
        }
        if (!List.of("普通", "白银", "黄金", "铂金", "钻石").contains(level)) {
            throw new BusinessException("等级值无效，可选：普通/白银/黄金/铂金/钻石");
        }

        user.setMemberLevel(level);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    // ==================== 积分调整 ====================
    /**
     * 调整会员积分（增加或减少）
     * <p>正数=增加积分，负数=减少积分（FIFO 扣减，按获得时间最早的先扣）</p>
     *
     * @param id     会员用户 ID
     * @param amount 调整数量（正数增加，负数减少）
     * @param reason 调整原因（可选）
     * @throws BusinessException 会员不存在、调整值为0、积分不足时抛出
     */
    @Transactional
    public void adjustPoints(Long id, Integer amount, String reason) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("会员不存在");
        }
        if (amount == null || amount == 0) {
            throw new BusinessException("调整积分不能为0");
        }

        if (amount > 0) {
            // 增加积分
            increasePoints(user, amount, reason);
        } else {
            // 减少积分（需要 FIFO 扣减）
            int deductAmount = Math.abs(amount);
            if (user.getPoints() < deductAmount) {
                throw new BusinessException("积分不足，当前积分：" + user.getPoints());
            }
            decreasePoints(user, deductAmount, reason);
        }
    }

    /**
     * 增加积分 —— 更新用户积分余额并记录积分流水
     *
     * @param user   会员用户对象
     * @param amount 增加数量
     * @param reason 增加原因
     */
    private void increasePoints(User user, Integer amount, String reason) {
        // 1. 更新用户积分余额
        userMapper.addPoints(user.getId(), amount);

        // 2. 记录积分流水
        PointTransaction tx = new PointTransaction();
        tx.setUserId(user.getId());
        tx.setType("管理员调整");
        tx.setChangeAmount(amount);
        tx.setBalanceAfter(user.getPoints() + amount);
        tx.setRemainAmount(amount);
        tx.setExpireTime(LocalDateTime.now().plusYears(1));
        tx.setDescription(reason != null ? reason : "管理员调增积分");
        tx.setCreateTime(LocalDateTime.now());
        tx.setUpdateTime(LocalDateTime.now());
        pointTransactionMapper.insert(tx);
    }

    /**
     * 减少积分（FIFO 先进先出扣减）
     * <p>按积分获得时间升序，从最早的批次开始扣减，直到扣完为止</p>
     *
     * @param user   会员用户对象
     * @param amount 扣减数量
     * @param reason 扣减原因
     */
    private void decreasePoints(User user, Integer amount, String reason) {
        // 1. 获取可用积分批次（按获得时间升序，最早的先扣）
        List<PointTransaction> batches = pointTransactionMapper.selectAvailableBatches(user.getId());

        int remaining = amount;
        int newBalance = user.getPoints();

        // 2. 按 FIFO 顺序扣减
        for (PointTransaction batch : batches) {
            if (remaining <= 0) break;

            int deduct = Math.min(remaining, batch.getRemainAmount());

            // 更新批次的剩余量
            batch.setRemainAmount(batch.getRemainAmount() - deduct);
            batch.setUpdateTime(LocalDateTime.now());
            pointTransactionMapper.updateById(batch);

            // 记录扣减流水
            PointTransaction tx = new PointTransaction();
            tx.setUserId(user.getId());
            tx.setType("管理员调整");
            tx.setChangeAmount(-deduct);
            tx.setBalanceAfter(newBalance - deduct);
            tx.setRemainAmount(0);
            tx.setBatchTxId(batch.getId());
            tx.setDescription(reason != null ? reason : "管理员调减积分");
            tx.setCreateTime(LocalDateTime.now());
            tx.setUpdateTime(LocalDateTime.now());
            pointTransactionMapper.insert(tx);

            remaining -= deduct;
            newBalance -= deduct;
        }

        // 3. 更新用户积分余额
        userMapper.deductPoints(user.getId(), amount);
    }

    // ==================== 重置密码 ====================
    /**
     * 管理员重置会员密码
     * <p>重置后：删除所有 refreshToken → 设置密码修改时间戳，使旧 Token 失效</p>
     *
     * @param id          会员用户 ID
     * @param newPassword 新密码（不允许为空）
     * @throws BusinessException 会员不存在或新密码为空时抛出
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("会员不存在");
        }

        // 如果没传新密码，生成一个默认密码
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BusinessException("新密码未输入");
        }

        // 加密并更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 强制下线：删除 Refresh Token + 设置密码修改时间戳（使旧 Token 失效）
        // 用户重新登录后，新 Token 的签发时间 > pw_changed 时间，可以正常使用
        refreshTokenMapper.deleteByUserId(id);
        redisUtil.set("pw_changed:" + id, String.valueOf(System.currentTimeMillis()), 604800);
    }
}
