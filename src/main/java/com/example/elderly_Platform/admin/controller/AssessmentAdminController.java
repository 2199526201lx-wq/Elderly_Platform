package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.AssessmentAdminService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.entity.Question;
import com.example.elderly_Platform.core.entity.Questionnaire;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端健康评测控制器 —— 管理员对评测问卷和题目进行管理
 * <p>仅管理员角色可访问（需在请求头携带管理员 Token）</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/assessment")
public class AssessmentAdminController {
    private final AssessmentAdminService assessmentAdminService;

    /**
     * 分页查询问卷列表
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @param status   问卷状态（可选：草稿/已发布/已下架）
     * @return 分页的问卷列表
     */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String status) {
        return Result.success(assessmentAdminService.list(pageNum, pageSize, status));
    }

    /**
     * 获取问卷详情
     *
     * @param id 问卷 ID
     * @return 问卷基本信息
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Questionnaire questionnaire = assessmentAdminService.getById(id);
        return Result.success(questionnaire);
    }

    /**
     * 获取问卷下的所有题目
     *
     * @param id 问卷 ID
     * @return 该问卷的题目列表（含选项信息）
     */
    @GetMapping("/{id}/questions")
    public Result<?> getQuestions(@PathVariable Long id) {
        return Result.success(assessmentAdminService.getQuestions(id));
    }

    /**
     * 创建问卷
     *
     * @param questionnaire 问卷信息（标题、描述、状态等）
     * @return 创建成功后返回空数据
     */
    @PostMapping
    public Result<?> create(@RequestBody Questionnaire questionnaire) {
        assessmentAdminService.create(questionnaire);
        return Result.success();
    }

    /**
     * 编辑问卷
     *
     * @param id            问卷 ID
     * @param questionnaire 修改后的问卷信息
     * @return 更新成功后返回空数据
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Questionnaire questionnaire) {
        assessmentAdminService.update(id, questionnaire);
        return Result.success();
    }

    /**
     * 删除问卷
     *
     * @param id 问卷 ID
     * @return 删除成功后返回空数据
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        assessmentAdminService.delete(id);
        return Result.success();
    }

    /**
     * 发布/下架问卷
     *
     * @param id     问卷 ID
     * @param status 目标状态（已发布/已下架）
     * @return 操作成功后返回空数据
     */
    @PutMapping("/status/{id}")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        assessmentAdminService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 为指定问卷添加题目
     *
     * @param id       问卷 ID
     * @param question 题目信息（题干、类型、选项、分值等）
     * @return 添加成功后返回空数据
     */
    @PostMapping("/{id}/question")
    public Result<?> addQuestion(@PathVariable Long id, @RequestBody Question question) {
        assessmentAdminService.addQuestion(id, question);
        return Result.success();
    }

    /**
     * 编辑题目
     *
     * @param questionId 题目 ID
     * @param question   修改后的题目信息
     * @return 更新成功后返回空数据
     */
    @PutMapping("/question/{questionId}")
    public Result<?> updateQuestion(@PathVariable Long questionId, @RequestBody Question question) {
        assessmentAdminService.updateQuestion(questionId, question);
        return Result.success();
    }

    /**
     * 删除题目
     *
     * @param questionId 题目 ID
     * @return 删除成功后返回空数据
     */
    @DeleteMapping("/question/{questionId}")
    public Result<?> deleteQuestion(@PathVariable Long questionId) {
        assessmentAdminService.deleteQuestion(questionId);
        return Result.success();
    }
}
