package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.MemberService;
import com.example.elderly_Platform.core.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class MemberController {
    private final MemberService memberService;

    /**
     * 会员列表（分页+筛选）
     */
    @GetMapping
    public Result<?> listMembers(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String realName,
                                 @RequestParam(required = false) String status) {
        return Result.success(memberService.listMembers(pageNum, pageSize, phone, realName, status));
    }

    /**
     * 会员详情
     */
    @GetMapping("/{id}")
    public Result<?> getMemberDetail(@PathVariable Long id) {
        return Result.success(memberService.getMemberDetail(id));
    }

    /**
     * 启用/禁用
     */
    @PutMapping("/status/{id}")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        memberService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 等级调整
     */
    @PutMapping("/level/{id}")
    public Result<?> updateLevel(@PathVariable Long id, @RequestParam String level) {
        memberService.updateLevel(id, level);
        return Result.success();
    }

    /**
     * 积分调整（amount 正数=增加，负数=减少）
     */
    @PutMapping("/points/{id}")
    public Result<?> adjustPoints(@PathVariable Long id,
                                   @RequestParam Integer amount,
                                   @RequestParam(required = false) String reason) {
        memberService.adjustPoints(id, amount, reason);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PutMapping("/password/{id}")
    public Result<?> resetPassword(@PathVariable Long id,
                                    @RequestParam(required = false) String newPassword) {
        memberService.resetPassword(id, newPassword);
        return Result.success();
    }
}
