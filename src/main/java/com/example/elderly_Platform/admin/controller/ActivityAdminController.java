package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.ActivityAdminService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.entity.CommunityActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端社区活动控制器 —— 管理员对社区活动进行增删改查及报名管理
 * <p>仅管理员角色可访问（需在请求头携带管理员 Token）</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/activity")
public class ActivityAdminController {
    private final ActivityAdminService activityAdminService;

    /**
     * 分页查询活动列表（支持按标题、状态筛选）
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @param title    活动标题（可选，模糊匹配）
     * @param status   活动状态（可选：报名中/进行中/已结束/已取消）
     * @return 分页的活动列表
     */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String title,
                           @RequestParam(required = false) String status) {
        return Result.success(activityAdminService.list(pageNum, pageSize, title, status));
    }

    /**
     * 获取活动详情
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(activityAdminService.getById(id));
    }

    /**
     * 创建活动
     *
     * @param activity 活动信息（标题、时间、地点、人数上限等）
     * @return 创建成功后返回空数据
     */
    @PostMapping
    public Result<?> create(@RequestBody CommunityActivity activity) {
        activityAdminService.create(activity);
        return Result.success();
    }

    /**
     * 编辑活动
     *
     * @param id       活动 ID
     * @param activity 修改后的活动信息
     * @return 更新成功后返回空数据
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody CommunityActivity activity) {
        activityAdminService.update(id, activity);
        return Result.success();
    }

    /**
     * 删除活动
     *
     * @param id 活动 ID
     * @return 删除成功后返回空数据
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        activityAdminService.delete(id);
        return Result.success();
    }

    /**
     * 查看某活动的报名列表
     *
     * @param id 活动 ID
     * @return 该活动所有报名记录（含报名会员信息、签到状态）
     */
    @GetMapping("/{id}/registrations")
    public Result<?> listRegistrations(@PathVariable Long id) {
        return Result.success(activityAdminService.listRegistrations(id));
    }
}
