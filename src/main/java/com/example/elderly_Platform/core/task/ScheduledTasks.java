package com.example.elderly_Platform.core.task;

import com.example.elderly_Platform.core.entity.PointTransaction;
import com.example.elderly_Platform.core.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 * 使用 Spring 的 @Scheduled 注解实现定时任务
 * Cron 表达式格式：秒 分 时 日 月 周
 * 例如：0 0 2 * * ? 表示每天凌晨 2 点执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final SmsCodeMapper smsCodeMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final UserMapper userMapper;
    private final AiConversationMessageMapper messageMapper;
    private final AiConversationSessionMapper sessionMapper;

    // ==================== 1. 清理过期短信验证码 ====================
    /**
     * 每 10 分钟执行一次
     * 业务逻辑：
     * - 短信验证码表（sms_code）没有 deleted 字段，采用物理删除
     * - 删除所有 expire_time < 当前时间 的记录
     * 为什么需要清理：
     * - 验证码通常只有几分钟有效期
     * - 过期验证码没有用处，占用数据库空间
     * - 定时清理可以保持数据库整洁
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void cleanExpiredSmsCodes() {
        log.info("开始清理过期短信验证码...");
        try {
            int count = smsCodeMapper.deleteExpired();
            log.info("清理完成，共删除 {} 条过期验证码", count);
        } catch (Exception e) {
            log.error("清理过期短信验证码失败", e);
        }
    }

    // ==================== 2. 清理过期积分 ====================
    /**
     * 每天凌晨 2 点执行
     * 业务逻辑：
     * 1. 查询所有已过期的积分批次（remain_amount > 0 且 expire_time < 当前时间）
     * 2. 对每个过期批次：
     *    a. 将 remain_amount 设为 0（标记为已过期）
     *    b. 生成一条"积分过期"类型的流水记录
     *    c. 扣减用户的积分余额
     * 为什么需要清理：
     * - 根据文档 5.8 节，积分获得后 1 年有效
     * - 过期积分应该自动清除，不能继续使用
     * 注意事项：
     * - 使用 @Transactional 保证事务一致性
     * - 如果某条记录处理失败，记录日志但继续处理其他记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredPoints() {
        log.info("开始清理过期积分...");
        try {
            // 查询所有已过期的积分批次
            List<PointTransaction> expiredBatches = pointTransactionMapper.selectExpiredBatches();
            log.info("发现 {} 个过期积分批次", expiredBatches.size());

            int totalCleaned = 0;

            for (PointTransaction batch : expiredBatches) {
                try {
                    // 1. 记录要过期的积分数量
                    int expiredAmount = batch.getRemainAmount();

                    // 2. 将批次的剩余量设为 0
                    batch.setRemainAmount(0);
                    batch.setUpdateTime(LocalDateTime.now());
                    pointTransactionMapper.updateById(batch);

                    // 3. 生成过期流水记录
                    PointTransaction expireTx = new PointTransaction();
                    expireTx.setUserId(batch.getUserId());
                    expireTx.setType("积分过期");
                    expireTx.setChangeAmount(-expiredAmount);  // 负数表示扣减
                    expireTx.setBalanceAfter(userMapper.selectById(batch.getUserId()).getPoints() - expiredAmount);
                    expireTx.setRemainAmount(0);
                    expireTx.setBatchTxId(batch.getId());
                    expireTx.setDescription("积分过期自动清理");
                    expireTx.setCreateTime(LocalDateTime.now());
                    expireTx.setUpdateTime(LocalDateTime.now());
                    pointTransactionMapper.insert(expireTx);

                    // 4. 扣减用户积分余额
                    userMapper.deductPoints(batch.getUserId(), expiredAmount);

                    totalCleaned++;
                } catch (Exception e) {
                    log.error("处理过期积分批次失败，批次ID: {}", batch.getId(), e);
                }
            }

            log.info("过期积分清理完成，成功处理 {} 个批次", totalCleaned);
        } catch (Exception e) {
            log.error("清理过期积分失败", e);
        }
    }

    // ==================== 3. 清理过期 AI 对话消息 ====================
    /**
     * 每天凌晨 3 点执行
     * 业务逻辑：
     * - 物理删除超过 6 个月的对话消息
     * - 根据文档 6.9.3 节，AI 对话消息保留 6 个月
     * 为什么需要清理：
     * - AI 对话消息表数据量增长快
     * - 用户通常不需要查看很久以前的对话
     * - 定期清理可以控制数据库大小
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredAiMessages() {
        log.info("开始清理过期 AI 对话消息...");
        try {
            int count = messageMapper.deleteExpiredMessages();
            log.info("清理完成，共删除 {} 条过期对话消息", count);
        } catch (Exception e) {
            log.error("清理过期 AI 对话消息失败", e);
        }
    }

    // ==================== 4. 清理过期 AI 对话会话列表 ====================
    /**
     * 每天凌晨 3 点执行（在清理消息之后）
     * 业务逻辑：
     * - 物理删除已逻辑删除（deleted=1）超过 6 个月的会话
     * - 根据文档 6.9.3 节，已删除的会话保留 6 个月后物理清理
     * 为什么需要清理：
     * - 用户删除会话后，数据仍然占用空间
     * - 保留 6 个月是为了数据恢复的可能性
     * - 超过 6 个月后物理删除释放空间
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredAiSessions() {
        log.info("开始清理过期 AI 对话会话...");
        try {
            int count = sessionMapper.deleteExpiredSessions();
            log.info("清理完成，共删除 {} 个过期会话", count);
        } catch (Exception e) {
            log.error("清理过期 AI 对话会话失败", e);
        }
    }
}
