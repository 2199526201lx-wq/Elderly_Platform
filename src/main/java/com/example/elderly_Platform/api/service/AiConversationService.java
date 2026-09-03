package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.dto.SendMessageDTO;
import com.example.elderly_Platform.core.entity.AiConversationMessage;
import com.example.elderly_Platform.core.entity.AiConversationSession;
import com.example.elderly_Platform.core.entity.HealthRecord;
import com.example.elderly_Platform.core.entity.User;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.AiConversationMessageMapper;
import com.example.elderly_Platform.core.mapper.AiConversationSessionMapper;
import com.example.elderly_Platform.core.mapper.HealthRecordMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.example.elderly_Platform.core.vo.MessageListVO;
import com.example.elderly_Platform.core.vo.SessionListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationService {
    /**
     * AI 对话服务 —— 处理与 AI 的会话创建、消息发送、流式对话等业务逻辑。
     * <p>
     * 负责维护 AI 对话会话（创建、查询、删除）以及会话内消息（发送、获取、流式输出），
     * 并基于 Spring AI 的 {@link ChatModel} 调用大模型生成回复。
     */
    private final AiConversationSessionMapper aiConversationSessionMapper;
    private final AiConversationMessageMapper aiConversationMessageMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final UserMapper userMapper;
    private final ChatModel chatModel;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /** 最多注入对话的最近健康记录条数 */
    private static final int HEALTH_RECORD_LIMIT = 10;

    /**
     * 创建新的 AI 对话会话
     *
     * @param userId 当前登录用户 ID
     * @return 新创建的会话对象（包含自动生成的会话 ID）
     */
    public AiConversationSession createSession(Long userId) {
        AiConversationSession session = new AiConversationSession();
        session.setUserId(userId);
        session.setSessionName(null);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setDeleted(0);
        aiConversationSessionMapper.insert(session);
        return session;
    }

    /**
     * 发送消息并获取 AI 回复（非流式，一次性返回完整结果）
     * <p>
     * 该方法会保存用户消息，调用 AI 大模型生成回复并保存，同时更新会话的更新时间。
     * 历史对话（全部）会作为上下文拼接到 Prompt 中。
     *
     * @param userId 当前登录用户 ID
     * @param dto    发送消息的数据传输对象，包含会话 ID 和消息内容
     * @return AI 回复的消息实体
     */
    public AiConversationMessage sendMessage(Long userId, @NonNull SendMessageDTO dto) {
        AiConversationMessage userMessage = new AiConversationMessage();
        userMessage.setUserId(userId);
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setRole("USER");
        userMessage.setMessage(dto.getContent());
        userMessage.setCreateTime(LocalDateTime.now());
        userMessage.setUpdateTime(LocalDateTime.now());
        userMessage.setDeleted(0);
        aiConversationMessageMapper.insert(userMessage);

        AiConversationMessage message = new AiConversationMessage();
        message.setSessionId(dto.getSessionId());
        message.setDeleted(0);
        List<AiConversationMessage> all=aiConversationMessageMapper.selectByCondition(message);

        StringBuilder prompt=new StringBuilder();
        prompt.append("你是一个友好的 AI 助手，专门为老年人提供帮助，并结合用户的健康记录进行回答。\n\n");
        // 注入用户的健康记录作为上下文
        prompt.append(buildHealthContext(userId));
        if(!all.isEmpty()){
            prompt.append("历史对话:\n");
            for(AiConversationMessage message1:all){
                String roleName="USER".equals(message1.getRole())?"用户":"AI";
                prompt.append(roleName).append(": ").append(message1.getMessage()).append("\n\n");
            }
        }
        prompt.append("当前消息:\n");
        prompt.append(("用户:")).append(dto.getContent()).append("\n\n");
        prompt.append("请结合以上用户的健康记录回答：");

        Prompt promptResult = new Prompt(prompt.toString());
        ChatResponse response = chatModel.call(promptResult);
        String aiReply = response.getResult().getOutput().getText();

        AiConversationMessage msg = new AiConversationMessage();
        msg.setSessionId(dto.getSessionId());
        msg.setUserId(userId);
        msg.setRole("AI");
        msg.setMessage(aiReply);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        msg.setDeleted(0);
        aiConversationMessageMapper.insert(msg);

        AiConversationSession session = new AiConversationSession();
        session.setId(dto.getSessionId());
        session.setUpdateTime(LocalDateTime.now());
        aiConversationSessionMapper.updateById(session);

        return msg;
    }

    /**
     * 获取指定用户的 AI 对话会话列表
     * <p>
     * 返回该用户所有未删除的会话，按更新时间倒序排列。
     *
     * @param userId 当前登录用户 ID
     * @return 会话列表视图对象，包含会话 ID、会话名称和更新时间
     */
    public List<SessionListVO> getSessionList(Long userId) {
        AiConversationSession aiConversationSession=new AiConversationSession();
        aiConversationSession.setUserId(userId);
        aiConversationSession.setDeleted(0);
        List<AiConversationSession> sessions=aiConversationSessionMapper.selectByCondition(aiConversationSession);
        return sessions.stream().map(session->{
            SessionListVO vo=new SessionListVO();
            vo.setSessionID(session.getId());
            vo.setSessionName(session.getSessionName());
            vo.setUpdateTime(session.getUpdateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取指定会话的消息列表
     * <p>
     * 返回该会话下所有未删除的消息，按创建时间正序排列（即对话时间顺序）。
     *
     * @param sessionId 会话 ID
     * @return 消息列表视图对象，包含消息 ID、角色（USER/AI）、内容和创建时间
     */
    public List<MessageListVO> getMessageList(Long sessionId) {
        AiConversationMessage aiConversationMessage=new AiConversationMessage();
        aiConversationMessage.setDeleted(0);
        aiConversationMessage.setSessionId(sessionId);
        List<AiConversationMessage> messages=aiConversationMessageMapper.selectByCondition(aiConversationMessage);
        return messages.stream().map(message->{
            MessageListVO vo=new MessageListVO();
            vo.setMessageId(message.getId());
            vo.setRole(message.getRole());
            vo.setCreateTime(message.getCreateTime());
            vo.setContent(message.getMessage());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 删除指定的 AI 对话会话及其所有消息
     * <p>
     * 删除前会校验会话是否存在以及当前用户是否有权限操作。
     * 删除操作会同时清理会话下的所有消息记录。
     *
     * @param sessionId 要删除的会话 ID
     * @param userId    当前登录用户 ID（用于权限校验）
     * @throws BusinessException 会话不存在或无权限删除时抛出
     */
    public void deleteSession(Long sessionId, Long userId) {
        AiConversationSession session=aiConversationSessionMapper.selectById(sessionId);
        if(session==null){
            throw new BusinessException("会话不存在");
        }
        if(!session.getUserId().equals(userId)){
            throw new BusinessException("无权删除该会话");
        }
        aiConversationSessionMapper.deleteById(sessionId);

        AiConversationMessage deleteMessage=new  AiConversationMessage();
        deleteMessage.setSessionId(sessionId);
        List<AiConversationMessage> messages=aiConversationMessageMapper.selectByCondition(deleteMessage);
        for(AiConversationMessage msg:messages){
            aiConversationMessageMapper.deleteById(msg.getId());
        }
    }

    /**
     * 发送消息并获取 AI 回复（SSE 流式输出，实现打字机效果）
     * <p>
     * 服务端通过 {@link SseEmitter} 向客户端持续推送 AI 生成的文本片段，
     * 客户端可逐字渲染以实现打字机效果。
     * <p>
     * SSE 事件类型说明：
     * <ul>
     *   <li>{@code message} — 流式文本片段，消费者需逐条追加到界面</li>
     *   <li>{@code error}   — 错误信息，AI 服务不可用时触发</li>
     *   <li>{@code done}    — 流结束标记，消费者收到后可做收尾处理</li>
     * </ul>
     * <p>
     * 历史对话最多取最近 10 轮（20 条）作为上下文；首条消息发送后会自动生成会话名称。
     *
     * @param userId 当前登录用户 ID
     * @param dto    发送消息的数据传输对象，包含会话 ID 和消息内容
     * @return SseEmitter 实例，超时时间设置为 5 分钟
     */
    public SseEmitter streamMessage(Long userId, SendMessageDTO dto) {
        // 1. 保存用户消息
        AiConversationMessage userMessage = new AiConversationMessage();
        userMessage.setUserId(userId);
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setRole("USER");
        userMessage.setMessage(dto.getContent());
        userMessage.setCreateTime(LocalDateTime.now());
        userMessage.setUpdateTime(LocalDateTime.now());
        userMessage.setDeleted(0);
        aiConversationMessageMapper.insert(userMessage);

        // 2. 构建 Prompt（复用历史对话）
        AiConversationMessage condition = new AiConversationMessage();
        condition.setSessionId(dto.getSessionId());
        condition.setDeleted(0);
        List<AiConversationMessage> history = aiConversationMessageMapper.selectByCondition(condition);

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个友好的 AI 助手，专门为老年人提供帮助，并结合用户的健康记录进行回答。\n\n");
        // 注入用户的健康记录作为上下文（在异步线程中使用最终复用，需先构建好）
        prompt.append(buildHealthContext(userId));
        if (!history.isEmpty()) {
            prompt.append("历史对话:\n");
            // 只取最近 10 轮对话（20 条消息）
            int start = Math.max(0, history.size() - 20);
            for (int i = start; i < history.size(); i++) {
                AiConversationMessage msg = history.get(i);
                String roleName = "USER".equals(msg.getRole()) ? "用户" : "AI";
                prompt.append(roleName).append(": ").append(msg.getMessage()).append("\n\n");
            }
        }
        prompt.append("当前消息:\n");
        prompt.append("用户:").append(dto.getContent()).append("\n\n");
        prompt.append("请结合用户的健康记录回答：");

        // 3. 创建 SseEmitter（5 分钟超时）
        SseEmitter emitter = new SseEmitter(300000L);

        // 4. 在异步线程中调用 AI 流式接口
        executor.execute(() -> {
            StringBuilder fullReply = new StringBuilder();
            try {
                Prompt promptObj = new Prompt(prompt.toString());
                Flux<ChatResponse> flux = chatModel.stream(promptObj);

                flux.subscribe(
                        chatResponse -> {
                            try {
                                String text = chatResponse.getResult().getOutput().getText();
                                if (text != null && !text.isEmpty()) {
                                    fullReply.append(text);
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(text));
                                }
                            } catch (IOException e) {
                                log.warn("SSE 发送中断，客户端可能已断开", e);
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("AI 流式调用失败", error);
                            // 保存失败标记的 AI 消息
                            saveAiMessage(dto.getSessionId(), userId,
                                    "AI 调用失败：" + error.getMessage());
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("AI 服务暂时不可用，请稍后重试"));
                            } catch (IOException ignored) {}
                            emitter.completeWithError(error);
                        },
                        () -> {
                            // 流结束：保存完整的 AI 回复
                            saveAiMessage(dto.getSessionId(), userId, fullReply.toString());

                            // 更新会话时间
                            AiConversationSession session = new AiConversationSession();
                            session.setId(dto.getSessionId());
                            session.setUpdateTime(LocalDateTime.now());
                            aiConversationSessionMapper.updateById(session);

                            // 如果是首条消息，生成会话名称
                            generateSessionNameIfNeeded(dto.getSessionId());

                            try {
                                emitter.send(SseEmitter.event().name("done").data(""));
                            } catch (IOException ignored) {}
                            emitter.complete();
                        }
                );
            } catch (Exception e) {
                log.error("流式对话异常", e);
                saveAiMessage(dto.getSessionId(), userId, "AI 调用异常：" + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 构建用户的健康记录上下文，注入到 AI 对话 Prompt 中
     * <p>
     * 查询当前用户的最近健康记录（最新 {@value #HEALTH_RECORD_LIMIT} 条），
     * 连同用户基础信息（性别、年龄、身高）拼接成一段上下文文本。
     * 无健康记录时不注入，仅返回身份提示。供 AI 结合健康数据回答健康类问题。
     *
     * @param userId 当前登录用户 ID
     * @return 健康记录上下文文本，始终非空
     */
    private String buildHealthContext(Long userId) {
        StringBuilder sb = new StringBuilder();

        // 1. 用户基础信息
        User user = userMapper.selectById(userId);
        StringBuilder profile = new StringBuilder();
        if (user != null) {
            if (user.getGender() != null && !user.getGender().isEmpty()) {
                profile.append("性别：").append(user.getGender()).append("；");
            }
            if (user.getBirthDate() != null) {
                int age = Period.between(user.getBirthDate().toLocalDate(), LocalDate.now()).getYears();
                profile.append("年龄：").append(age).append("岁；");
            }
            if (user.getHeight() != null) {
                profile.append("身高：").append(user.getHeight().stripTrailingZeros().toPlainString()).append("cm；");
            }
        }
        sb.append("【用户基础信息】").append(profile.length() > 0 ? profile : "未填写").append("\n\n");

        // 2. 最近健康记录
        HealthRecord condition = new HealthRecord();
        condition.setUserId(userId);
        condition.setDeleted(0);
        List<HealthRecord> records = healthRecordMapper.selectByCondition(condition);

        if (records == null || records.isEmpty()) {
            sb.append("【用户健康记录】暂无健康记录，请基于常识回答日常健康问题。\n\n");
            return sb.toString();
        }

        // 只取最近的 HEALTH_RECORD_LIMIT 条（mapper 已按 recorded_time 倒序）
        int limit = Math.min(records.size(), HEALTH_RECORD_LIMIT);
        sb.append("【用户健康记录】（最新在前，最多展示 ").append(limit).append(" 条）\n");
        for (int i = 0; i < limit; i++) {
            HealthRecord r = records.get(i);
            sb.append(i + 1).append(". 时间：").append(r.getRecordedTime()).append("，");
            if (r.getSystolic() != null || r.getDiastolic() != null) {
                sb.append("血压：").append(nvl(r.getSystolic())).append("/").append(nvl(r.getDiastolic())).append(" mmHg，");
            }
            if (r.getBloodSugar() != null) {
                sb.append("空腹血糖：").append(r.getBloodSugar()).append(" mmol/L，");
            }
            if (r.getHeartRate() != null) {
                sb.append("心率：").append(r.getHeartRate()).append(" 次/分，");
            }
            if (r.getWeight() != null) {
                sb.append("体重：").append(r.getWeight()).append(" kg，");
            }
            if (r.getBmi() != null) {
                sb.append("BMI：").append(r.getBmi()).append("，");
            }
            if (r.getMemo() != null && !r.getMemo().isEmpty()) {
                sb.append("备注：").append(r.getMemo());
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private static String nvl(Integer value) {
        return value != null ? String.valueOf(value) : "—";
    }

    /**
     * 保存 AI 回复消息
     */
    private void saveAiMessage(Long sessionId, Long userId, String content) {
        AiConversationMessage msg = new AiConversationMessage();
        msg.setSessionId(sessionId);
        msg.setUserId(userId);
        msg.setRole("AI");
        msg.setMessage(content);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        msg.setDeleted(0);
        aiConversationMessageMapper.insert(msg);
    }

    /**
     * 如果会话还没有名称（首条消息），用 AI 生成简短标题
     */
    private void generateSessionNameIfNeeded(Long sessionId) {
        AiConversationSession session = aiConversationSessionMapper.selectById(sessionId);
        if (session != null && (session.getSessionName() == null || session.getSessionName().isEmpty())) {
            // 获取第一条用户消息
            AiConversationMessage condition = new AiConversationMessage();
            condition.setSessionId(sessionId);
            condition.setRole("USER");
            condition.setDeleted(0);
            List<AiConversationMessage> msgs = aiConversationMessageMapper.selectByCondition(condition);
            if (!msgs.isEmpty()) {
                String firstMsg = msgs.getFirst().getMessage();
                String name;
                try {
                    Prompt namePrompt = new Prompt("请用不超过20个字概括以下消息的主题，直接返回标题文字，不要加引号或多余符号：\n" + firstMsg);
                    ChatResponse nameResp = chatModel.call(namePrompt);
                    name = nameResp.getResult().getOutput().getText();
                    if (name != null && name.length() > 20) {
                        name = name.substring(0, 20);
                    }
                } catch (Exception e) {
                    // 兜底：取前 20 字
                    name = firstMsg.length() > 20 ? firstMsg.substring(0, 20) : firstMsg;
                }
                AiConversationSession update = new AiConversationSession();
                update.setId(sessionId);
                update.setSessionName(name);
                update.setUpdateTime(LocalDateTime.now());
                aiConversationSessionMapper.updateById(update);
            }
        }
    }
}
