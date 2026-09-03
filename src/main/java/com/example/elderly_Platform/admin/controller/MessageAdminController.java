package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.MessageAdminService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.BatchMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端消息控制器 —— 管理员发送、查询、删除用户消息通知
 * <p>仅管理员角色可访问（需在请求头携带管理员 Token）</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/message")
public class MessageAdminController {
    private final MessageAdminService messageAdminService;

    /**
     * 分页查询消息列表
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @param type     消息类型（可选：系统通知/活动提醒/健康提醒等）
     * @param isRead   是否已读（可选：0=未读，1=已读）
     * @return 分页的消息列表
     */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String type,
                           @RequestParam(required = false) Integer isRead) {
        return Result.success(messageAdminService.list(pageNum, pageSize, type, isRead));
    }

    /**
     * 获取消息详情
     *
     * @param id 消息 ID
     * @return 消息详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(messageAdminService.getById(id));
    }

    /**
     * 向单个用户发送消息
     *
     * @param userId  接收消息的用户 ID
     * @param title   消息标题
     * @param content 消息内容
     * @param type    消息类型（可选）
     * @return 发送成功后返回空数据
     */
    @PostMapping
    public Result<?> send(@RequestParam Long userId,
                           @RequestParam String title,
                           @RequestParam String content,
                           @RequestParam(required = false) String type) {
        messageAdminService.send(userId, title, content, type);
        return Result.success();
    }

    /**
     * 向多个用户批量发送消息
     *
     * @param dto 批量发送信息：userIds（用户ID列表）、title（标题）、content（内容）、type（类型）
     * @return 发送成功后返回空数据
     */
    @PostMapping("/batch")
    public Result<?> sendBatch(@RequestBody BatchMessageDTO dto) {
        messageAdminService.sendBatch(dto.getUserIds(), dto.getTitle(), dto.getContent(), dto.getType());
        return Result.success();
    }

    /**
     * 删除消息
     *
     * @param id 消息 ID
     * @return 删除成功后返回空数据
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        messageAdminService.delete(id);
        return Result.success();
    }
}
