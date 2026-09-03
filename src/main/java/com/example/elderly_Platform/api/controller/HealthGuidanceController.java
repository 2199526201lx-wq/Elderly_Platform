package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.HealthGuidanceService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.entity.HealthGuidance;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 健康指导控制器 —— 为会员提供个性化健康指导文章/建议
 * <p>根据用户的健康档案数据，系统会生成或推荐相应的健康指导内容</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/health-guidance")
public class HealthGuidanceController {
    private final HealthGuidanceService healthGuidanceService;

    /**
     * 分页查询健康指导列表
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @param type     指导类型（可选筛选条件，如：饮食/运动/用药等）
     * @return 分页的健康指导列表
     */
    @GetMapping("/list")
    public Result<?> getHealthGuidanceLists(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String type) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageInfo<HealthGuidance> list = healthGuidanceService.getHealthGuidanceLists(userId, pageNum, pageSize, type);
        return Result.success(list);
    }

    /**
     * 获取健康指导详情
     *
     * @param id 健康指导文章 ID
     * @return 健康指导详情（包含完整内容）
     */
    @GetMapping("/{id}")
    public Result<?> getHealthGuidanceDetails(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HealthGuidance healthGuidance = healthGuidanceService.getHealthGuidanceDetails(id, userId);
        return Result.success(healthGuidance);
    }

    /**
     * 标记健康指导为已读
     *
     * @param id 健康指导文章 ID
     * @return 标记结果（1=成功，0=失败）
     */
    @PutMapping("/read/{id}")
    public Result<?> isRead(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer i = healthGuidanceService.isRead(id, userId);
        return Result.success(i);
    }

    /**
     * 获取未读健康指导数量
     *
     * @return 当前用户未读的健康指导文章数量
     */
    @GetMapping("/unread/count")
    public Result<?> getUnreadCount() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer i = healthGuidanceService.unReadCount(userId);
        return Result.success(i);
    }
}
