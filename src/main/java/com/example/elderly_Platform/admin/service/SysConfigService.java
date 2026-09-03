package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.entity.SysConfig;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端系统配置服务 —— 处理系统配置项的查询、更新等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class SysConfigService {
    private final SysConfigMapper sysConfigMapper;

    /**
     * 查询所有系统配置项列表
     *
     * @return 系统配置列表
     */
    // ==================== 配置列表 ====================
    public List<SysConfig> list() {
        return sysConfigMapper.selectByCondition(new SysConfig());
    }

    /**
     * 根据配置ID查询单个系统配置
     *
     * @param id 配置ID
     * @return 系统配置实体
     * @throws BusinessException 当配置项不存在时抛出异常
     */
    // ==================== 获取单个配置 ====================
    public SysConfig getById(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("配置项不存在");
        }
        return config;
    }

    /**
     * 更新系统配置项
     * <p>配置值和描述均可选，仅更新提供的字段</p>
     *
     * @param id          配置ID
     * @param configValue 配置值（可选）
     * @param description 配置描述（可选）
     * @throws BusinessException 当配置项不存在时抛出异常
     */
    // ==================== 更新配置 ====================
    public void update(Long id, String configValue, String description) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("配置项不存在");
        }

        if (configValue != null) {
            config.setConfigValue(configValue);
        }
        if (description != null) {
            config.setDescription(description);
        }
        config.setUpdateTime(LocalDateTime.now());
        sysConfigMapper.updateById(config);
    }
}
