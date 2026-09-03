package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.HealthRecordAdminService;
import com.example.elderly_Platform.core.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端健康档案控制器 —— 管理员查看会员的健康记录和健康趋势
 * <p>仅管理员角色可访问（需在请求头携带管理员 Token）</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/health-record")
public class HealthRecordAdminController {
    private final HealthRecordAdminService healthRecordAdminService;

    /**
     * 分页查询指定会员的健康记录
     *
     * @param userId   会员 ID
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @return 分页的健康记录列表（血压、血糖、心率等数据）
     */
    @GetMapping
    public Result<?> listByUser(@RequestParam Long userId,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(healthRecordAdminService.listByUser(userId, pageNum, pageSize));
    }

    /**
     * 查询指定会员的健康趋势
     *
     * @param userId 会员 ID
     * @param months 趋势分析的月份数，默认 6
     * @return 健康趋势数据（各指标随时间的走势）
     */
    @GetMapping("/trend")
    public Result<?> trend(@RequestParam Long userId,
                            @RequestParam(defaultValue = "6") Integer months) {
        return Result.success(healthRecordAdminService.trend(userId, months));
    }
}
