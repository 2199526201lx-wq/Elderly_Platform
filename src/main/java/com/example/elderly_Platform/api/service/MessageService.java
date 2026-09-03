package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.entity.Message;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.MessageMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内消息服务 —— 管理用户的站内消息（健康提醒、通知等）。
 * <p>
 * 提供消息的未读计数、分页查询、详情查看以及已读标记等功能，
 * 消息主要由系统根据健康指标异常自动生成，类型通常为"健康提醒"。
 */
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageMapper messageMapper;

    /**
     * 统计用户未读消息的数量
     *
     * @param userId 当前登录用户 ID
     * @return 未读消息数量
     */
    public Integer countUnread(Long userId){
        Message message=new Message();
        message.setUserId(userId);
        message.setIsRead(0);
        return messageMapper.selectByCondition(message).size();
    }
    /**
     * 分页查询用户的站内消息列表
     *
     * @param userId    当前登录用户 ID
     * @param pageNum   页码（从 1 开始）
     * @param pageSize  每页条数
     * @return 分页结果对象，包含消息列表及分页信息
     */
    public PageInfo<Message> list(Long userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Message message=new Message();
        message.setUserId(userId);
        List<Message> messageList=messageMapper.selectByCondition(message);
        return new PageInfo<>(messageList);
    }
    /**
     * 获取消息详情并自动标记为已读
     * <p>
     * 查询消息前会校验消息是否存在以及当前用户是否有权限查看。
     * 若消息尚未标记为已读（isRead 为 0），则自动更新为已读状态。
     *
     * @param userId    当前登录用户 ID（用于权限校验）
     * @param messageId 消息 ID
     * @return 消息实体对象
     * @throws BusinessException 消息不存在或无权限查看时抛出
     */
    public Message detail(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看此消息");
        }
        if (message.getIsRead() == 0) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
        return message;
    }
    /**
     * 将指定消息标记为已读
     * <p>
     * 查询消息前会校验消息是否存在以及当前用户是否有权限操作。
     *
     * @param userId    当前登录用户 ID（用于权限校验）
     * @param messageId 消息 ID
     * @throws BusinessException 消息不存在或无权限操作时抛出
     */
    public void markRead(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此消息");
        }
        message.setIsRead(1);
        messageMapper.updateById(message);
    }
}
