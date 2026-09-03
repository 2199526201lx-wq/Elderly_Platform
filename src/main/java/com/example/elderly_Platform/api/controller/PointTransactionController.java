package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.PointTransactionService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.entity.PointTransaction;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 积分流水控制器 —— 查询会员的积分获取/消费记录
 * <p>积分来源包括：注册赠送、活动签到、管理员调整、消费扣减等</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class PointTransactionController {
    private final PointTransactionService pointTransactionService;

    /**
     * 分页查询积分流水记录
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @param type     流水类型（可选筛选，如：注册赠送/管理员调整/消费等）
     * @return 分页的积分流水列表（按创建时间倒序，包含变动金额和变动后余额）
     */
    @GetMapping("/points")
    public Result<?> selectPoints(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String type) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageInfo<PointTransaction> list = pointTransactionService.selectPoints(pageNum, pageSize, type, userId);
        return Result.success(list);
    }
}
