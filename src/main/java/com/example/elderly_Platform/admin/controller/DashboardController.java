package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.DashboardService;
import com.example.elderly_Platform.core.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端数据看板控制器 —— 提供首页统计概览数据
 * <p>仅管理员角色可访问（需在请求头携带管理员 Token）</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * 获取看板统计数据摘要
     * <p>返回管理员首页所需的各类统计指标，如会员总数、活动数、预约数、今日新增等</p>
     *
     * @return 统计摘要数据（DashboardVO）
     */
    @GetMapping("/summary")
    public Result<?> summary() {
        return Result.success(dashboardService.getSummary());
    }
}