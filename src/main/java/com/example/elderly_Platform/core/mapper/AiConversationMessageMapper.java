package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.AiConversationMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiConversationMessageMapper {
    AiConversationMessage selectById(Long id);

    List<AiConversationMessage> selectByCondition(AiConversationMessage message);

    int insert(AiConversationMessage message);

    int updateById(AiConversationMessage message);

    int deleteById(Long id);

    /**
     * 物理删除超过 6 个月的对话消息（定时任务使用）
     */
    int deleteExpiredMessages();
}
