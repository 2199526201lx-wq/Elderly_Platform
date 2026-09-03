package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.entity.Question;
import com.example.elderly_Platform.core.entity.Questionnaire;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.QuestionMapper;
import com.example.elderly_Platform.core.mapper.QuestionnaireMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端评估问卷服务 —— 处理评估问卷及题目的增删改查、发布/下架等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AssessmentAdminService {
    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;

    /**
     * 分页查询评估问卷列表（支持按状态筛选）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   问卷状态（可选，如：草稿、已发布）
     * @return 分页的问卷列表 PageInfo
     */
    // ==================== 问卷列表（分页） ====================
    public PageInfo<Questionnaire> list(Integer pageNum, Integer pageSize, String status) {
        PageHelper.startPage(pageNum, pageSize);

        Questionnaire condition = new Questionnaire();
        condition.setStatus(status);

        List<Questionnaire> list = questionnaireMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    /**
     * 根据问卷ID查询问卷详情
     *
     * @param id 问卷ID
     * @return 问卷实体
     * @throws BusinessException 当问卷不存在时抛出异常
     */
    // ==================== 问卷详情（含题目） ====================
    public Questionnaire getById(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }
        return questionnaire;
    }

    /**
     * 查询指定问卷的所有题目
     *
     * @param questionnaireId 问卷ID
     * @return 题目列表
     */
    public List<Question> getQuestions(Long questionnaireId) {
        Question condition = new Question();
        condition.setQuestionnaireId(questionnaireId);
        return questionMapper.selectByCondition(condition);
    }

    /**
     * 创建新的评估问卷
     * <p>创建后问卷状态默认为"草稿"</p>
     *
     * @param questionnaire 问卷实体
     */
    // ==================== 创建问卷 ====================
    public void create(Questionnaire questionnaire) {
        questionnaire.setStatus("草稿");
        questionnaireMapper.insert(questionnaire);
    }

    /**
     * 更新评估问卷信息
     *
     * @param id            问卷ID
     * @param questionnaire 问卷实体（包含需要更新的字段）
     * @throws BusinessException 当问卷不存在或问卷已发布时抛出异常
     */
    // ==================== 编辑问卷 ====================
    public void update(Long id, Questionnaire questionnaire) {
        Questionnaire existing = questionnaireMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("问卷不存在");
        }
        if ("已发布".equals(existing.getStatus())) {
            throw new BusinessException("已发布的问卷不能编辑，请先下架");
        }

        questionnaire.setId(id);
        questionnaireMapper.updateById(questionnaire);
    }

    /**
     * 删除评估问卷
     *
     * @param id 问卷ID
     * @throws BusinessException 当问卷不存在时抛出异常
     */
    // ==================== 删除问卷 ====================
    public void delete(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }
        questionnaireMapper.deleteById(id);
    }

    /**
     * 更新问卷状态（发布/下架）
     *
     * @param id     问卷ID
     * @param status 目标状态（"草稿" 或 "已发布"）
     * @throws BusinessException 当问卷不存在或状态值无效时抛出异常
     */
    // ==================== 发布/下架问卷 ====================
    public void updateStatus(Long id, String status) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }
        if (!"草稿".equals(status) && !"已发布".equals(status)) {
            throw new BusinessException("状态值无效，只能是'草稿'或'已发布'");
        }

        questionnaire.setStatus(status);
        questionnaire.setUpdateTime(LocalDateTime.now());
        questionnaireMapper.updateById(questionnaire);
    }

    /**
     * 向问卷中添加题目
     *
     * @param questionnaireId 问卷ID
     * @param question        题目实体
     * @throws BusinessException 当问卷不存在或问卷已发布时抛出异常
     */
    // ==================== 添加题目 ====================
    public void addQuestion(Long questionnaireId, Question question) {
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }
        if ("已发布".equals(questionnaire.getStatus())) {
            throw new BusinessException("已发布的问卷不能添加题目，请先下架");
        }

        question.setQuestionnaireId(questionnaireId);
        questionMapper.insert(question);
    }

    /**
     * 更新题目信息
     *
     * @param questionId 题目ID
     * @param question   题目实体（包含需要更新的字段）
     * @throws BusinessException 当题目不存在或所属问卷已发布时抛出异常
     */
    // ==================== 编辑题目 ====================
    public void updateQuestion(Long questionId, Question question) {
        Question existing = questionMapper.selectById(questionId);
        if (existing == null) {
            throw new BusinessException("题目不存在");
        }

        // 检查所属问卷是否已发布
        Questionnaire questionnaire = questionnaireMapper.selectById(existing.getQuestionnaireId());
        if (questionnaire != null && "已发布".equals(questionnaire.getStatus())) {
            throw new BusinessException("已发布的问卷不能编辑题目，请先下架");
        }

        question.setId(questionId);
        questionMapper.updateById(question);
    }

    /**
     * 删除题目
     *
     * @param questionId 题目ID
     * @throws BusinessException 当题目不存在或所属问卷已发布时抛出异常
     */
    // ==================== 删除题目 ====================
    public void deleteQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        // 检查所属问卷是否已发布
        Questionnaire questionnaire = questionnaireMapper.selectById(question.getQuestionnaireId());
        if (questionnaire != null && "已发布".equals(questionnaire.getStatus())) {
            throw new BusinessException("已发布的问卷不能删除题目，请先下架");
        }

        questionMapper.deleteById(questionId);
    }
}
