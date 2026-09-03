package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.entity.Message;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.MessageMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端消息服务 —— 处理消息的查询、发送、批量发送及删除等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class MessageAdminService {
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询消息列表（支持按类型、已读状态筛选）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param type     消息类型（可选，如：系统、通知等）
     * @param isRead   是否已读（可选，0-未读，1-已读）
     * @return 分页的消息列表 PageInfo
     */
    // ==================== 消息列表（分页） ====================
    public PageInfo<Message> list(Integer pageNum, Integer pageSize, String type, Integer isRead) {
        PageHelper.startPage(pageNum, pageSize);

        Message condition = new Message();

        condition.setType(type);
        condition.setIsRead(isRead);

        List<Message> list = messageMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    /**
     * 根据消息ID查询消息详情
     *
     * @param id 消息ID
     * @return 消息实体
     * @throws BusinessException 当消息不存在时抛出异常
     */
    // ==================== 消息详情 ====================
    public Message getById(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        return message;
    }

    /**
     * 向单个用户发送消息
     * <p>消息类型默认为"系统"</p>
     *
     * @param userId  接收用户ID
     * @param title   消息标题
     * @param content 消息内容
     * @param type    消息类型（可选，默认"系统"）
     * @throws BusinessException 当用户不存在时抛出异常
     */
    // ==================== 发送单个消息 ====================
    public void send(Long userId, String title, String content, String type) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }

        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type != null ? type : "系统");
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    /**
     * 向多个用户批量发送消息
     * <p>消息类型默认为"系统"</p>
     *
     * @param userIds 接收用户ID列表
     * @param title   消息标题
     * @param content 消息内容
     * @param type    消息类型（可选，默认"系统"）
     * @throws BusinessException 当用户列表为空时抛出异常
     */
    // ==================== 批量发送消息 ====================
    public void sendBatch(List<Long> userIds, String title, String content, String type) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户列表不能为空");
        }

        String msgType = type != null ? type : "系统";

        for (Long userId : userIds) {
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setType(msgType);
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            messageMapper.insert(message);
        }
    }

    /**
     * 删除消息（逻辑删除）
     *
     * @param id 消息ID
     * @throws BusinessException 当消息不存在时抛出异常
     */
    // ==================== 删除消息（逻辑删除） ====================
    public void delete(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        messageMapper.deleteById(id);
    }
}
