package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.dto.AiScoreResult;
import com.example.elderly_Platform.core.dto.AssessmentSubmitDTO;
import com.example.elderly_Platform.core.dto.json.OptionJSON;
import com.example.elderly_Platform.core.entity.AssessmentResult;
import com.example.elderly_Platform.core.entity.PointTransaction;
import com.example.elderly_Platform.core.entity.Question;
import com.example.elderly_Platform.core.entity.Questionnaire;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.*;
import com.example.elderly_Platform.core.util.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 健康评测服务 —— 处理问卷列表、题目查询、评测历史、提交评测（AI评分）等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AssessmentService {
    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final UserMapper userMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final AiService aiService;

    /**
     * 查询所有已发布的问卷列表
     *
     * @return 已发布问卷列表
     */
    public List<Questionnaire> listPublished() {
        Questionnaire qa = new Questionnaire();
        qa.setStatus("已发布");
        return questionnaireMapper.selectByCondition(qa);
    }

    /**
     * 获取指定问卷的所有题目列表
     *
     * @param questionnaireId 问卷ID
     * @return 题目列表
     */
    public List<Question> getList(Long questionnaireId) {
        Question qt = new Question();
        qt.setQuestionnaireId(questionnaireId);
        return questionMapper.selectByCondition(qt);
    }

    /**
     * 查询指定用户的评测历史记录
     *
     * @param userId 用户ID
     * @return 评测历史记录列表
     */
    public List<AssessmentResult> getHistory(Long userId) {
        AssessmentResult ar = new AssessmentResult();
        ar.setUserId(userId);
        return assessmentResultMapper.selectByCondition(ar);
    }

    /**
     * 查看评测详情（含权限校验：只能查看自己的）
     *
     * @param userId 用户ID
     * @param id     评测记录ID
     * @return 评测详情
     * @throws BusinessException 当评测记录不存在或无权查看时抛出异常
     */
    public AssessmentResult getDetail(Long userId, Long id) {
        AssessmentResult result = assessmentResultMapper.selectById(id);
        if (result == null) {
            throw new BusinessException("评测记录不存在");
        }
        if (!result.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看该评测记录");
        }
        return result;
    }

    /**
     * 提交评测
     * <p>校验问卷状态、按规则计算得分并调用 AI 评分生成建议，保存评测结果并赠送20积分、记录积分流水</p>
     *
     * @param userId 用户ID
     * @param dto    评测提交数据（问卷ID及答案列表）
     * @return 保存后的评测结果
     * @throws BusinessException 当问卷不存在、未发布或题目为空时抛出异常
     */
    @Transactional
    public AssessmentResult submit(Long userId, AssessmentSubmitDTO dto) {
        Questionnaire qr = questionnaireMapper.selectById(dto.getQuestionnaireId());
        if (qr == null) {
            throw new BusinessException("问卷不存在");
        }
        if (!("已发布").equals(qr.getStatus())) {
            throw new BusinessException("问卷未发布");
        }

        Question q = new Question();
        q.setQuestionnaireId(dto.getQuestionnaireId());
        List<Question> questions = questionMapper.selectByCondition(q);
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException(500, "题目未加载出来（实际题目为空或不存在）");
        }

        Map<Long, Question> questionMap = new HashMap<>();
        for (Question question : questions) {
            questionMap.put(question.getId(), question);
        }

        BigDecimal totalScore = BigDecimal.ZERO;


        for (AssessmentSubmitDTO.AnswerItem answer : dto.getAnswers()) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }
            if ("计分".equals(question.getScoreMode()) && !"文本".equals(question.getType())) {
                List<OptionJSON> options = JSONUtil.fromJson(
                        question.getOptions(),
                        new TypeReference<>() {
                        }
                );

                BigDecimal score = BigDecimal.ZERO;
                if (answer.getSelectedTexts() != null) {
                    for (String selectedText : answer.getSelectedTexts()) {
                        for (OptionJSON qj : options) {
                            if (selectedText.equals(qj.getText())) {
                                score = score.add(qj.getScore());
                                break;
                            }
                        }
                    }
                }
                if (question.getMaxScore() != null && score.compareTo(BigDecimal.valueOf(question.getMaxScore())) > 0) {
                    score = BigDecimal.valueOf(question.getMaxScore());

                }
                totalScore = totalScore.add(score);
            }
        }

        int selectedScore = totalScore.divide(BigDecimal.valueOf(qr.getTotalScore()), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).intValue();
        AiScoreResult aiResult = aiService.assess(qr, questions, dto.getAnswers(), selectedScore);

        String answersJson = JSONUtil.toJson(dto.getAnswers());
        AssessmentResult result = new AssessmentResult();
        result.setUserId(userId);
        result.setQuestionnaireId(dto.getQuestionnaireId());
        result.setAnswers(answersJson);
        result.setRuleScore(selectedScore);
        result.setAiScore(aiResult.getAiScore());
        result.setAiSuggestion(aiResult.getAiSuggestion());
        result.setCreateTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());
        assessmentResultMapper.insert(result);

        userMapper.addPoints(userId, 20);
        PointTransaction pt = new PointTransaction();
        pt.setUserId(userId);
        pt.setType("评测完成");
        pt.setChangeAmount(20);
        pt.setBalanceAfter(userMapper.selectById(userId).getPoints());
        pt.setRemainAmount(20);
        pt.setExpireTime(LocalDateTime.now().plusYears(1));
        pt.setDescription("完成测评" + qr.getTitle());
        pt.setRefId(result.getId());
        pointTransactionMapper.insert(pt);

        return result;


    }
}