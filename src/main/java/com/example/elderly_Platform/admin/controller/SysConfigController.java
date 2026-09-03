package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.SysConfigService;
import com.example.elderly_Platform.core.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端系统配置控制器 —— 管理系统运行参数（如公告、积分规则等）
 * <p>仅管理员角色可访问（需在请求头携带管理员 Token）</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/config")
public class SysConfigController {
    private final SysConfigService sysConfigService;

    /**
     * 查询全部系统配置项
     *
     * @return 系统配置列表
     */
    @GetMapping
    public Result<?> list() {
        return Result.success(sysConfigService.list());
    }

    /**
     * 获取单个配置项
     *
     * @param id 配置项 ID
     * @return 配置项详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(sysConfigService.getById(id));
    }

    /**
     * 更新配置项
     *
     * @param id          配置项 ID
     * @param configValue 新的配置值（可选）
     * @param description 新的配置描述（可选）
     * @return 更新成功后返回空数据
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id,
                             @RequestParam(required = false) String configValue,
                             @RequestParam(required = false) String description) {
        sysConfigService.update(id, configValue, description);
        return Result.success();
    }
}
