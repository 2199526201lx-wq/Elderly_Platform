package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.entity.HealthRecord;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.HealthRecordMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.example.elderly_Platform.core.vo.HealthRecordVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端健康记录服务 —— 处理会员健康记录查询及健康趋势分析等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class HealthRecordAdminService {
    private final HealthRecordMapper healthRecordMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询指定会员的健康记录
     *
     * @param userId   会员ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页的健康记录列表 PageInfo
     * @throws BusinessException 当会员不存在时抛出异常
     */
    // ==================== 会员健康记录分页查询 ====================
    public PageInfo<HealthRecord> listByUser(Long userId, Integer pageNum, Integer pageSize) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("会员不存在");
        }

        PageHelper.startPage(pageNum, pageSize);

        HealthRecord condition = new HealthRecord();
        condition.setUserId(userId);

        List<HealthRecord> list = healthRecordMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    /**
     * 查询指定会员的健康趋势
     *
     * @param userId 会员ID
     * @param months 统计月份数（可选，默认最近6个月）
     * @return 包含健康统计指标的健康趋势 VO
     * @throws BusinessException 当会员不存在时抛出异常
     */
    // ==================== 会员健康趋势查询 ====================
    public HealthRecordVO trend(Long userId, Integer months) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("会员不存在");
        }
        if (months == null || months <= 0) {
            months = 6;
        }

        LocalDateTime startTime = LocalDate.now().minusMonths(months).atStartOfDay();
        return healthRecordMapper.selectStats(userId, startTime);
    }
}
