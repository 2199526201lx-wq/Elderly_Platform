package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.AiConversationService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.SendMessageDTO;
import com.example.elderly_Platform.core.entity.AiConversationMessage;
import com.example.elderly_Platform.core.entity.AiConversationSession;
import com.example.elderly_Platform.core.vo.MessageListVO;
import com.example.elderly_Platform.core.vo.SessionListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 智能对话控制器 —— 为会员提供 AI 健康咨询对话服务
 * <p>支持普通消息对话和 SSE 流式对话（打字机效果），对话内容由 AI 服务生成</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member/ai")
public class AiConversationController {
    private final AiConversationService aiConversationService;

    /**
     * 1. 创建会话
     */
    @PostMapping("/session")
    public Result<?> createSession(){
        Long userId=(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AiConversationSession session=aiConversationService.createSession(userId);
        return Result.success(session);
    }

    /**
     * 2. 发送消息（非流式，同步返回完整回复）
     */
    @PostMapping("/message")
    public Result<?> sendMessage(@RequestBody @Valid SendMessageDTO dto){
        Long userId=(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AiConversationMessage message=aiConversationService.sendMessage(userId,dto);
        return Result.success(message);
    }

    /**
     * 3. 流式对话（SSE，打字机效果）
     * 前端使用 fetch + ReadableStream 消费，事件类型：
     *   - "message" : AI 回复的每个文字片段
     *   - "done"    : 回复结束
     *   - "error"   : 出错信息
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(@RequestBody @Valid SendMessageDTO dto){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return aiConversationService.streamMessage(userId, dto);
    }

    /**
     * 4. 会话列表
     */
    @GetMapping("/sessionlist")
    public Result<?> getSessionList(){
        Long userId=(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<SessionListVO> sessionList=aiConversationService.getSessionList(userId);
        return Result.success(sessionList);
    }

    /**
     * 5. 消息历史
     */
    @GetMapping("/messages/{sessionId}")
    public Result<?> getMessageList(@PathVariable Long sessionId) {
        List<MessageListVO> messages = aiConversationService.getMessageList(sessionId);
        return Result.success(messages);
    }

    /**
     * 6. 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<?> deleteSession(@PathVariable Long sessionId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        aiConversationService.deleteSession(sessionId, userId);
        return Result.success();
    }
}
