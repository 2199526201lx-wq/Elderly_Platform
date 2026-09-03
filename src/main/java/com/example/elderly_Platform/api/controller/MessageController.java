package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.MessageService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.entity.Message;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 消息控制器 —— 处理会员的消息通知功能
 * <p>消息类型包括：系统通知、活动提醒、健康提醒、预约提醒等</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member/message")
public class MessageController {
    private final MessageService messageService;

    /**
     * 获取未读消息数量
     *
     * @return 当前用户的未读消息数量
     */
    @GetMapping("/unread/count")
    public Result<?> getUnreadMessages() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer i = messageService.countUnread(userId);
        return Result.success(i);
    }

    /**
     * 分页查询消息列表
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @return 分页的消息列表（按创建时间倒序）
     */
    @GetMapping("/list")
    public Result<?> listMessages(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageInfo<Message> pageInfo = messageService.list(userId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取消息详情
     *
     * @param id 消息 ID
     * @return 消息详情（查看后自动标记为已读）
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(messageService.detail(userId, id));
    }

    /**
     * 标记消息为已读
     *
     * @param id 消息 ID
     * @return 标记成功后返回空数据
     */
    @PutMapping("/read/{id}")
    public Result<?> markRead(@PathVariable Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        messageService.markRead(userId, id);
        return Result.success();
    }
}
