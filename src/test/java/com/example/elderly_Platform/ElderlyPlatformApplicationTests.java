package com.example.elderly_Platform;

import com.example.elderly_Platform.api.service.AiConversationService;  // ✅ 改成正确的类名
import com.example.elderly_Platform.core.dto.SendMessageDTO;
import com.example.elderly_Platform.core.entity.AiConversationMessage;
import com.example.elderly_Platform.core.entity.AiConversationSession;
import com.example.elderly_Platform.core.vo.MessageListVO;
import com.example.elderly_Platform.core.vo.SessionListVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiConversationServiceTest {  // ✅ 改成正确的类名

    @Autowired
    private AiConversationService aiConversationService;  // ✅ 改成正确的变量名

    // 测试用户 ID（替换成你数据库中存在的用户 ID）
    private static final Long TEST_USER_ID = 1L;

    @Test
    void testCreateSession() {
        // 创建会话
        AiConversationSession session = aiConversationService.createSession(TEST_USER_ID);

        // 验证
        assertNotNull(session);
        assertNotNull(session.getId());
        assertEquals(TEST_USER_ID, session.getUserId());
        assertNull(session.getSessionName());

        System.out.println("✅ 创建会话成功，ID: " + session.getId());
    }

    @Test
    void testSendMessage() {
        // 1. 先创建会话
        AiConversationSession session = aiConversationService.createSession(TEST_USER_ID);
        Long sessionId = session.getId();

        // 2. 发送消息
        SendMessageDTO dto = new SendMessageDTO();
        dto.setSessionId(sessionId);
        dto.setContent("你好，请问今天天气怎么样？");

        AiConversationMessage message = aiConversationService.sendMessage(TEST_USER_ID, dto);

        // 验证
        assertNotNull(message);
        assertEquals("AI", message.getRole());
        assertNotNull(message.getMessage());

        System.out.println("✅ 发送消息成功");
        System.out.println("用户消息：你好，请问今天天气怎么样？");
        System.out.println("AI 回复：" + message.getMessage());
    }

    @Test
    void testGetSessionList() {
        // 获取会话列表
        List<SessionListVO> sessions = aiConversationService.getSessionList(TEST_USER_ID);

        // 验证
        assertNotNull(sessions);

        System.out.println("✅ 会话列表查询成功，共 " + sessions.size() + " 个会话");
        for (SessionListVO vo : sessions) {
            System.out.println("  - ID: " + vo.getSessionID() + ", 名称: " + vo.getSessionName());
        }
    }

    @Test
    void testGetMessageList() {
        // 1. 先创建会话并发送消息
        AiConversationSession session = aiConversationService.createSession(TEST_USER_ID);
        SendMessageDTO dto = new SendMessageDTO();
        dto.setSessionId(session.getId());
        dto.setContent("测试消息");
        aiConversationService.sendMessage(TEST_USER_ID, dto);

        // 2. 获取消息列表
        List<MessageListVO> messages = aiConversationService.getMessageList(session.getId());

        // 验证
        assertNotNull(messages);
        assertTrue(messages.size() >= 2); // 至少有用户消息和 AI 回复

        System.out.println("✅ 消息列表查询成功，共 " + messages.size() + " 条消息");
        for (MessageListVO vo : messages) {
            System.out.println("  [" + vo.getRole() + "] " + vo.getContent());
        }
    }

    @Test
    void testDeleteSession() {
        // 1. 创建会话
        AiConversationSession session = aiConversationService.createSession(TEST_USER_ID);
        Long sessionId = session.getId();

        // 2. 发送消息
        SendMessageDTO dto = new SendMessageDTO();
        dto.setSessionId(sessionId);
        dto.setContent("测试删除");
        aiConversationService.sendMessage(TEST_USER_ID, dto);

        // 3. 删除会话
        aiConversationService.deleteSession(sessionId, TEST_USER_ID);

        System.out.println("✅ 删除会话成功，ID: " + sessionId);
    }
}
