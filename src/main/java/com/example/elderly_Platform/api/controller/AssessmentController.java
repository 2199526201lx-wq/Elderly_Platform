package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.AssessmentService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.AssessmentSubmitDTO;
import com.example.elderly_Platform.core.entity.AssessmentResult;
import com.example.elderly_Platform.core.entity.Question;
import com.example.elderly_Platform.core.entity.Questionnaire;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 健康评测控制器 —— 处理会员的健康评测问卷相关功能
 * <p>评测问卷由管理员发布，会员完成评测后由 AI 打分并给出建议</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/assessment")
public class AssessmentController {
    private final AssessmentService assessmentService;

    /**
     * 获取所有已发布的评测问卷列表
     *
     * @return 已发布的问卷列表（不含题目详情，仅含问卷基本信息）
     */
    @GetMapping("/all")
    public Result<?> listPublished() {
        List<Questionnaire> list = assessmentService.listPublished();
        return Result.success(list);
    }

    /**
     * 获取指定问卷的所有题目
     *
     * @param questionnaireId 问卷 ID
     * @return 该问卷下的所有题目列表（含选项信息）
     */
    @GetMapping("/{questionnaireId}")
    public Result<?> getList(@PathVariable Long questionnaireId) {
        List<Question> list = assessmentService.getList(questionnaireId);
        return Result.success(list);
    }

    /**
     * 获取当前会员的评测历史记录
     *
     * @return 该会员所有已完成的评测结果列表（按评测时间倒序）
     */
    @GetMapping("/history")
    public Result<?> getHistory() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<AssessmentResult> list = assessmentService.getHistory(userId);
        return Result.success(list);
    }

    /**
     * 查看单条评测结果详情
     *
     * @param id 评测结果 ID
     * @return 评测结果详情（含 AI 评分、建议等）
     */
    @GetMapping("/result/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AssessmentResult result = assessmentService.getDetail(userId, id);
        return Result.success(result);
    }

    /**
     * 提交评测答案
     * <p>流程：保存评测结果 → 调用 AI 服务进行评分 → 返回评分结果和建议</p>
     *
     * @param dto 提交信息：questionnaireId（问卷ID）、answers（题目答案列表）
     * @return 评测结果（含总分、AI 建议等）
     */
    @PostMapping("/submit")
    public Result<?> submit(@Valid @RequestBody AssessmentSubmitDTO dto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AssessmentResult as = assessmentService.submit(userId, dto);
        return Result.success(as);
    }
}
