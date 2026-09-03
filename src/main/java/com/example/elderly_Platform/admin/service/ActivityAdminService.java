package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.entity.ActivityRegistration;
import com.example.elderly_Platform.core.entity.CommunityActivity;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.ActivityRegistrationMapper;
import com.example.elderly_Platform.core.mapper.CommunityActivityMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端社区活动服务 —— 处理社区活动的增删改查及报名信息查询等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ActivityAdminService {
    private final CommunityActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;

    /**
     * 分页查询活动列表（支持按标题、状态筛选）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param title    活动标题（可选，模糊匹配）
     * @param status   活动状态（可选，如：草稿、已发布等）
     * @return 分页的活动列表 PageInfo
     */
    public PageInfo<CommunityActivity> list(Integer pageNum, Integer pageSize, String title, String status) {
        PageHelper.startPage(pageNum, pageSize);

        CommunityActivity condition = new CommunityActivity();
        condition.setTitle(title);
        condition.setStatus(status);

        List<CommunityActivity> list = activityMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    /**
     * 根据活动ID查询活动详情
     *
     * @param id 活动ID
     * @return 活动实体
     * @throws BusinessException 当活动不存在时抛出异常
     */
    public CommunityActivity getById(Long id) {
        CommunityActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        return activity;
    }

    /**
     * 创建新的社区活动
     * <p>创建后活动状态默认为"草稿"，当前参与人数初始化为0</p>
     *
     * @param activity 活动实体（包含标题、内容、时间等信息）
     * @throws BusinessException 当活动时间逻辑校验不通过时抛出异常
     */
    // ==================== 创建活动 ====================
    public void create(CommunityActivity activity) {
        // 校验时间逻辑
        validateTime(activity);

        activity.setCurrentParticipants(0);
        // 状态由报名/活动时间自动推导，管理员不可手动指定
        activity.setStatus(autoStatus(activity));
        activityMapper.insert(activity);
    }

    /**
     * 更新社区活动信息
     *
     * @param id       活动ID
     * @param activity 活动实体（包含需要更新的字段）
     * @throws BusinessException 当活动不存在或时间逻辑校验不通过时抛出异常
     */
    // ==================== 编辑活动 ====================
    public void update(Long id, CommunityActivity activity) {
        CommunityActivity existing = activityMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("活动不存在");
        }

        // 未传的时间字段沿用已有值，保证自动状态计算正确
        if (activity.getRegistrationStartTime() == null) activity.setRegistrationStartTime(existing.getRegistrationStartTime());
        if (activity.getRegistrationEndTime() == null) activity.setRegistrationEndTime(existing.getRegistrationEndTime());
        if (activity.getActivityStartTime() == null) activity.setActivityStartTime(existing.getActivityStartTime());
        if (activity.getActivityEndTime() == null) activity.setActivityEndTime(existing.getActivityEndTime());
        if (activity.getMaxParticipants() == null) activity.setMaxParticipants(existing.getMaxParticipants());

        // 校验时间逻辑
        validateTime(activity);

        activity.setId(id);
        activity.setUpdateTime(LocalDateTime.now());
        // 状态由报名/活动时间自动推导，管理员仅改时间间接改变状态，不可手动指定
        activity.setStatus(autoStatus(activity));
        activityMapper.updateById(activity);
    }

    /**
     * 根据报名/活动时间自动推导活动状态（管理员不可手动改状态）
     * <p>规则：</p>
     * <ul>
     *   <li>活动已结束（当前时间 ≥ 活动结束时间）→ 已结束</li>
     *   <li>活动进行中（报名结束 ≤ 当前 < 活动结束）→ 进行中</li>
     *   <li>报名进行中（报名开始 ≤ 当前 < 报名结束）→ 报名中</li>
     *   <li>尚在报名开始之前 → 草稿</li>
     * </ul>
     */
    private String autoStatus(CommunityActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime regStart = activity.getRegistrationStartTime();
        LocalDateTime regEnd = activity.getRegistrationEndTime();
        LocalDateTime actStart = activity.getActivityStartTime();
        LocalDateTime actEnd = activity.getActivityEndTime();

        if (actEnd != null && (now.isAfter(actEnd) || now.isEqual(actEnd))) {
            return "已结束";
        }
        if (regEnd != null && actEnd != null && !now.isBefore(regEnd) && now.isBefore(actEnd)) {
            return "进行中";
        }
        if (regStart != null && regEnd != null && !now.isBefore(regStart) && now.isBefore(regEnd)) {
            return "报名中";
        }
        return "草稿";
    }

    /**
     * 删除社区活动（逻辑删除）
     *
     * @param id 活动ID
     * @throws BusinessException 当活动不存在时抛出异常
     */
    // ==================== 删除活动（逻辑删除） ====================
    public void delete(Long id) {
        CommunityActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        activityMapper.deleteById(id);
    }

    /**
     * 查看指定活动的报名人员列表
     *
     * @param activityId 活动ID
     * @return 该活动的报名记录列表
     * @throws BusinessException 当活动不存在时抛出异常
     */
    // ==================== 查看活动报名列表 ====================
    public List<ActivityRegistration> listRegistrations(Long activityId) {
        CommunityActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        ActivityRegistration condition = new ActivityRegistration();
        condition.setActivityId(activityId);
        return registrationMapper.selectByCondition(condition);
    }

    // ==================== 时间校验 ====================
    private void validateTime(CommunityActivity activity) {
        LocalDateTime regStart = activity.getRegistrationStartTime();
        LocalDateTime regEnd = activity.getRegistrationEndTime();
        LocalDateTime actStart = activity.getActivityStartTime();
        LocalDateTime actEnd = activity.getActivityEndTime();

        if (regStart != null && regEnd != null && regStart.isAfter(regEnd)) {
            throw new BusinessException("报名开始时间不能晚于报名结束时间");
        }
        if (actStart != null && actEnd != null && actStart.isAfter(actEnd)) {
            throw new BusinessException("活动开始时间不能晚于活动结束时间");
        }
        if (regEnd != null && actStart != null && regEnd.isAfter(actStart)) {
            throw new BusinessException("报名结束时间不能晚于活动开始时间");
        }
    }
}
