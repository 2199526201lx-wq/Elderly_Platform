package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.AiConversationSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiConversationSessionMapper {
    AiConversationSession selectById(Long id);

    List<AiConversationSession> selectByCondition(AiConversationSession session);

    int insert(AiConversationSession session);

    int updateById(AiConversationSession session);

    int deleteById(Long id);

    /**
     * 物理删除已逻辑删除超过 6 个月的会话（定时任务使用）
     */
    int deleteExpiredSessions();
}
