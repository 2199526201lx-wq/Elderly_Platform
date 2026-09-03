package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.entity.HealthGuidance;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.HealthGuidanceMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthGuidanceService {
    /**
     * 健康指导服务 —— 管理用户的健康指导信息。
     * <p>
     * 提供健康指导的分页查询、详情查看、已读标记以及未读计数等功能，
     * 健康指导由系统根据用户的健康指标异常自动生成。
     */
    private final HealthGuidanceMapper healthGuidanceMapper;

    /**
     * 分页查询用户的健康指导列表
     *
     * @param userId   当前登录用户 ID
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @param type     健康指导类型（如"数据小结"），可为空表示不过滤
     * @return 分页结果对象，包含健康指导列表及分页信息
     */
    public PageInfo<HealthGuidance> getHealthGuidanceLists(Long userId, Integer pageNum, Integer pageSize, String type) {
        PageHelper.startPage(pageNum, pageSize);
        HealthGuidance healthGuidance = new HealthGuidance();
        healthGuidance.setUserId(userId);
        healthGuidance.setType(type);
        List<HealthGuidance> list=healthGuidanceMapper.selectByCondition(healthGuidance);
        return new PageInfo<>(list);
    }
    /**
     * 获取健康指导的详细信息
     * <p>
     * 查询前会校验指导是否存在以及当前用户是否有权限查看。
     *
     * @param id     健康指导 ID
     * @param userId 当前登录用户 ID（用于权限校验）
     * @return 健康指导实体对象
     * @throws BusinessException 指导不存在或无权限访问时抛出
     */
    public HealthGuidance getHealthGuidanceDetails(Long id,Long userId){
        HealthGuidance healthGuidance=healthGuidanceMapper.selectById(id);
        if(healthGuidance==null){
            throw new BusinessException("健康指导说明不存在");
        }
        if(!userId.equals(healthGuidance.getUserId())){
            throw new BusinessException(403,"没有权限访问");
        }
        return healthGuidance;
    }
    /**
     * 将健康指导标记为已读
     * <p>
     * 仅当指导尚未标记已读（isRead 为 0）时才更新为已读。
     *
     * @param id     健康指导 ID
     * @param userId 当前登录用户 ID（用于权限校验）
     * @return 影响的行数（0 表示未发生更新）
     * @throws BusinessException 指导不存在或无权限操作时抛出
     */
    public Integer isRead(Long id,Long userId){
        HealthGuidance healthGuidance=healthGuidanceMapper.selectById(id);
        if(healthGuidance==null){
            throw new BusinessException("健康指导说明不存在");
        }
        if(!userId.equals(healthGuidance.getUserId())){
            throw new BusinessException(403,"没有权限访问");
        }
        if (healthGuidance.getIsRead()==0) {
            healthGuidance.setIsRead(1);
        }
        return healthGuidanceMapper.updateById(healthGuidance);
    }
    /**
     * 统计用户未读健康指导的数量
     *
     * @param userId 当前登录用户 ID
     * @return 未读健康指导的数量
     */
    public Integer unReadCount(Long userId){
        HealthGuidance healthGuidance=new HealthGuidance();
        healthGuidance.setUserId(userId);
        healthGuidance.setIsRead(0);
        return healthGuidanceMapper.selectByCondition(healthGuidance).size();
    }
}
