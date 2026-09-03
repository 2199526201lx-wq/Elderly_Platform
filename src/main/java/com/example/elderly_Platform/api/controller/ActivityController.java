package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.ActivityService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.entity.ActivityRegistration;
import com.example.elderly_Platform.core.entity.CommunityActivity;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 会员活动控制器 —— 处理会员浏览社区活动、报名、签到等功能
 * <p>需会员登录后访问（需在请求头携带会员 Token）</p>
 */
@RestController
@RequestMapping("/api/member/activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    /**
     * 活动列表（分页）
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String status){
        PageInfo<CommunityActivity> page = activityService.list(pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * 活动详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id){
        CommunityActivity activity = activityService.getDetail(id);
        return Result.success(activity);
    }

    /**
     * 我的活动（分页）
     */
    @GetMapping("/my")
    public Result<?> myActivities(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageInfo<ActivityRegistration> page = activityService.myActivities(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 查看某活动的签到状态
     */
    @GetMapping("/{id}/checkin-status")
    public Result<?> checkInStatus(@PathVariable Long id){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ActivityRegistration reg = activityService.checkInStatus(userId, id);
        return Result.success(reg);
    }

    /**
     * 活动报名
     */
    @PostMapping("/{id}/join")
    public Result<?> join(@PathVariable Long id){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.join(userId, id);
        return Result.success();
    }

    /**
     * 活动签到
     */
    @PostMapping("/{id}/checkin")
    public Result<?> checkin(@PathVariable Long id){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.checkIn(userId, id);
        return Result.success();
    }
}
