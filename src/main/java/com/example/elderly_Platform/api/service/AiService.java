package com.example.elderly_Platform.api.service;


import com.example.elderly_Platform.core.dto.AiScoreResult;
import com.example.elderly_Platform.core.dto.AssessmentSubmitDTO;
import com.example.elderly_Platform.core.dto.json.OptionJSON;
import com.example.elderly_Platform.core.entity.Question;
import com.example.elderly_Platform.core.entity.Questionnaire;
import com.example.elderly_Platform.core.util.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 智能评估服务 —— 调用大模型对问卷作答进行智能评分与建议生成。
 * <p>
 * 将问卷、题目、用户答案及规则分拼接为 Prompt，调用 AI 模型后解析返回的 JSON 结果，
 * 在 AI 调用或解析失败时提供降级兜底（使用规则分作为 AI 分）。
 */
@Service
@RequiredArgsConstructor
public class AiService {
    private final ChatModel chatModel;

    /**
     * 对用户问卷作答进行 AI 智能评分
     * <p>
     * 根据问卷、题目、用户答案及规则分构造 Prompt 并调用 AI 模型，
     * 解析模型返回的 JSON 得到 AI 评分与个性化建议。
     * 若 AI 调用或解析失败，则降级返回：AI 分使用规则分，建议中携带错误信息。
     *
     * @param questionnaire 问卷实体
     * @param questions     问卷包含的题目列表
     * @param answers       用户提交的作答项列表
     * @param ruleScore     规则计算的百分制得分
     * @return AI 评分结果，包含 AI 评分（aiScore）和个性化建议（aiSuggestion）
     */
    public AiScoreResult assess(Questionnaire questionnaire,
                                List<Question> questions,
                                List<AssessmentSubmitDTO.AnswerItem> answers,
                                Integer ruleScore){
        try {
            // 1. 构造 Prompt
            String prompt = buildPrompt(questionnaire, questions, answers, ruleScore);

            // 2. 调用 AI
            Prompt promptObj = new Prompt(prompt);
            ChatResponse response = chatModel.call(promptObj);
            String aiResponse = response.getResult().getOutput().getText();

            // 3. 解析响应
            return parseAiResponse(aiResponse);
        } catch (Exception e) {
            // AI 调用失败时，返回默认值
            AiScoreResult result = new AiScoreResult();
            result.setAiScore(ruleScore);  // 使用规则分作为 AI 分
            result.setAiSuggestion("AI 评分暂时不可用，请稍后重试。错误信息：" + e.getMessage());
            return result;
        }
    }

    private String buildPrompt(Questionnaire questionnaire,
                               List<Question> questions,
                               List<AssessmentSubmitDTO.AnswerItem> answers,
                               Integer ruleScore){
        StringBuilder sb=new StringBuilder();
        sb.append("你是一个健康评估专家。请根据以下问卷和用户的 answers，给出评分和建议。\n\n");
        sb.append("【问卷信息】\n");
        sb.append("标题：").append(questionnaire.getTitle()).append("\n");
        sb.append("描述：").append(questionnaire.getDescription()).append("\n\n");

        sb.append("【题目和答案】\n");
        Map<Long,Question> questionMap=new HashMap<>();
        for (Question question : questions) {
            questionMap.put(question.getId(), question);
        }
        int questionNum = 1;
        for(AssessmentSubmitDTO.AnswerItem answer:answers){
            Question q=questionMap.get(answer.getQuestionId());
            if(q==null ){
                continue;
            }
            sb.append(questionNum++).append(". ").append(q.getContent()).append("\n");

            if ("文本".equals(q.getType())) {
                // 文本题
                sb.append("类型：文本题（满分 ").append(q.getMaxScore()).append(" 分）\n");
                sb.append("用户回答：").append(answer.getText() != null ? answer.getText() : "无").append("\n");
                sb.append("AI 评分：请根据回答质量给出 0-").append(q.getMaxScore()).append(" 分\n");
            } else if ("单选".equals(q.getType())) {
                // 单选题
                sb.append("类型：单选题");
                if ("计分".equals(q.getScoreMode())) {
                    sb.append("（计分题，满分 ").append(q.getMaxScore()).append(" 分）\n");
                } else {
                    sb.append("（非计分题）\n");
                }

                // 解析选项
                List<OptionJSON> options = JSONUtil.fromJson(
                        q.getOptions(),
                        new TypeReference<>() {}
                );

                if (answer.getSelectedTexts() != null && !answer.getSelectedTexts().isEmpty()) {
                    String selectedText = answer.getSelectedTexts().getFirst();
                    for (OptionJSON option : options) {
                        if (selectedText.equals(option.getText())) {
                            sb.append("用户选择：").append(option.getText()).append("\n");
                            sb.append("选项意义：").append(option.getMeaning()).append("\n");
                            if ("计分".equals(q.getScoreMode()) && option.getScore() != null) {
                                sb.append("得分：").append(option.getScore()).append("/").append(q.getMaxScore()).append("\n");
                            }
                        }
                    }
                }
            } else if ("多选".equals(q.getType())) {
                // 多选题
                sb.append("类型：多选题");
                if ("计分".equals(q.getScoreMode())) {
                    sb.append("（计分题，满分 ").append(q.getMaxScore()).append(" 分，每选一个选项累加得分）\n");
                } else {
                    sb.append("（非计分题）\n");
                }

                // 解析选项
                List<OptionJSON> options = JSONUtil.fromJson(
                        q.getOptions(),
                        new TypeReference<>() {}
                );

                if (answer.getSelectedTexts() != null) {
                    BigDecimal totalOptionScore = BigDecimal.ZERO;
                    for (String selectedText : answer.getSelectedTexts()) {
                        for (OptionJSON option : options) {
                            if (selectedText.equals(option.getText())) {
                                sb.append("选项：").append(option.getText()).append("\n");
                                sb.append("  意义：").append(option.getMeaning()).append("\n");
                                if ("计分".equals(q.getScoreMode()) && option.getScore() != null) {
                                    sb.append("  该选项得分：").append(option.getScore()).append(" 分\n");
                                    totalOptionScore = totalOptionScore.add(option.getScore());
                                }
                            }
                        }
                    }
                    if ("计分".equals(q.getScoreMode())) {
                        int finalScore = Math.min(totalOptionScore.intValue(), q.getMaxScore());
                        sb.append("多选题总得分：").append(finalScore).append("/").append(q.getMaxScore()).append("\n");
                    }
                }
            }
            sb.append("\n");

        }

        sb.append("\n【规则分】\n");
        sb.append("用户规则分：").append(ruleScore).append(" 分（百分制）\n\n");
        sb.append("【输出要求】\n");
        sb.append("请返回严格的 JSON 对象（不要包含注释，不要包含任何多余文字，字段名必须与示例完全一致）：\n");
        sb.append("{\n");
        sb.append("  \"aiScore\": 80,\n");
        sb.append("  \"aiSuggestion\": \"您的整体健康状况良好，建议保持规律作息和适量运动，合理饮食\"\n");
        sb.append("}\n");
        sb.append("其中 aiScore 为 0-100 的整数百分制评分，aiSuggestion 为 200 字以内的个性化建议。");

        return sb.toString();
    }
    private AiScoreResult parseAiResponse(String aiResponse){
        try {
            // 去掉 AI 返回中可能夹带的行注释（//），避免污染 JSON
            String cleaned = aiResponse.replaceAll("(?m)//.*$", "");
            // AI 可能返回纯 JSON，也可能在 JSON 前后有文字
            // 先尝试直接解析
            try {
                return parseFromJson(cleaned);
            } catch (Exception e) {
                // 如果失败，尝试提取 JSON 部分
                int start = cleaned.indexOf('{');
                int end = cleaned.lastIndexOf('}');
                if (start != -1 && end != -1 && start < end) {
                    String json = cleaned.substring(start, end + 1);
                    return parseFromJson(json);
                }
                throw e;  // 都失败则抛出异常
            }
        } catch (Exception e) {
            // 解析失败，返回默认值
            AiScoreResult result = new AiScoreResult();
            result.setAiScore(null);
            result.setAiSuggestion(aiResponse);  // 把原始响应作为建议
            return result;
        }
    }

    /**
     * 将 JSON 转换为 AiScoreResult。
     * <p>同时兼容 aiScore / ai_score、aiSuggestion / ai_suggestion 两种字段命名，防止 AI 返回下划线命名时拿不到分数。</p>
     */
    private AiScoreResult parseFromJson(String json) {
        com.fasterxml.jackson.databind.JsonNode node = JSONUtil.fromJson(json, com.fasterxml.jackson.databind.JsonNode.class);
        AiScoreResult result = new AiScoreResult();
        com.fasterxml.jackson.databind.JsonNode scoreNode = node.get("aiScore") != null ? node.get("aiScore") : node.get("ai_score");
        com.fasterxml.jackson.databind.JsonNode suggestionNode = node.get("aiSuggestion") != null ? node.get("aiSuggestion") : node.get("ai_suggestion");
        if (scoreNode != null && !scoreNode.isNull() && (scoreNode.isNumber() || scoreNode.isTextual())) {
            result.setAiScore(scoreNode.asInt());
        }
        if (suggestionNode != null && !suggestionNode.isNull()) {
            result.setAiSuggestion(suggestionNode.asText());
        }
        return result;
    }

}
