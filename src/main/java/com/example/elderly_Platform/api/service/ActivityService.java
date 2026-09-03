package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.entity.ActivityRegistration;
import com.example.elderly_Platform.core.entity.CommunityActivity;
import com.example.elderly_Platform.core.entity.PointTransaction;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.ActivityRegistrationMapper;
import com.example.elderly_Platform.core.mapper.CommunityActivityMapper;
import com.example.elderly_Platform.core.mapper.PointTransactionMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员活动服务 —— 处理会员浏览活动、报名、签到等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ActivityService {
    private final UserMapper userMapper;
    private final CommunityActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final PointTransactionMapper pointTransactionMapper;

    /**
     * 分页查询活动列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   活动状态（可选）
     * @return 分页的活动列表 PageInfo
     */
    public PageInfo<CommunityActivity> list(Integer pageNum, Integer pageSize, String status) {
        PageHelper.startPage(pageNum, pageSize);
        CommunityActivity condition = new CommunityActivity();
        if (status != null && !status.isEmpty()) {
            condition.setStatus(status);
        }
        // 会员端不展示草稿活动
        return new PageInfo<>(activityMapper.selectForMember(condition));
    }

    /**
     * 获取活动详情
     *
     * @param id 活动ID
     * @return 活动详情实体
     * @throws BusinessException 当活动不存在时抛出异常
     */
    public CommunityActivity getDetail(Long id) {
        CommunityActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        // 草稿活动不对会员开放详情
        if ("草稿".equals(activity.getStatus())) {
            throw new BusinessException("活动不存在");
        }
        return activity;
    }

    /**
     * 分页查询我的活动报名记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页的活动报名记录 PageInfo
     */
    public PageInfo<ActivityRegistration> myActivities(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        ActivityRegistration condition = new ActivityRegistration();
        condition.setUserId(userId);
        List<ActivityRegistration> list = registrationMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    /**
     * 查看某用户在某活动中的签到状态
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 活动报名记录（含签到状态）
     * @throws BusinessException 当用户未报名该活动时抛出异常
     */
    public ActivityRegistration checkInStatus(Long userId, Long activityId) {
        ActivityRegistration reg = registrationMapper.selectByUserAndActivity(userId, activityId);
        if (reg == null) {
            throw new BusinessException("您未报名该活动");
        }
        return reg;
    }

    /**
     * 报名参加活动
     * <p>校验报名时间、是否已报名、活动名额等，报名成功后活动参与人数+1</p>
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @throws BusinessException 当活动不存在、不在报名时间、已报名或名额已满时抛出异常
     */
    @Transactional
    public void join(Long userId,Long activityId){
        CommunityActivity activity=activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        LocalDateTime now=LocalDateTime.now();
        if (now.isBefore(activity.getRegistrationStartTime())
                || now.isAfter(activity.getRegistrationEndTime())) {
            throw new BusinessException("不在报名时间内");
        }

        // 是否已报名（按用户+活动查询）
        ActivityRegistration existing = registrationMapper.selectByUserAndActivity(userId, activityId);
        if (existing != null) {
            throw new BusinessException("您已报名该活动");
        }

        CommunityActivity locked=activityMapper.selectForUpdate(activityId);
        if (locked.getCurrentParticipants() >= locked.getMaxParticipants()) {
            throw new BusinessException("活动名额已满");
        }

        // 插入报名记录
        ActivityRegistration reg=new ActivityRegistration();
        reg.setActivityId(activityId);
        reg.setUserId(userId);
        reg.setCheckInStatus("未签到");
        reg.setCreateTime(LocalDateTime.now());
        reg.setUpdateTime(LocalDateTime.now());
        registrationMapper.insert(reg);

        // 活动已报名人数 +1
        activityMapper.incrementCount(activityId);
    }

    /**
     * 活动签到
     * <p>校验是否已报名、是否在活动时间范围内、是否已签到，签到成功后赠送50积分并记录积分流水</p>
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @throws BusinessException 当未报名、不在活动时间、已签到或活动不存在时抛出异常
     */
    @Transactional
    public void checkIn(Long userId,Long activityId){
        ActivityRegistration reg = registrationMapper.selectByUserAndActivity(userId, activityId);
        if (reg == null) {
            throw new BusinessException("未报名该活动，无法签到");
        }

        CommunityActivity activity=activityMapper.selectById(activityId);
        LocalDateTime now=LocalDateTime.now();
        if (now.isBefore(activity.getActivityStartTime())
                || now.isAfter(activity.getActivityEndTime())) {
            throw new BusinessException("不在活动时间内，无法签到");
        }
        if ("已签到".equals(reg.getCheckInStatus())) {
            throw new BusinessException("您已签到过");
        }

        registrationMapper.updateCheckIn(now, userId, activityId);
        userMapper.addPoints(userId, 50);

        PointTransaction tx = new PointTransaction();
        tx.setUserId(userId);
        tx.setType("活动签到");
        tx.setChangeAmount(50);
        tx.setBalanceAfter(userMapper.selectById(userId).getPoints());
        tx.setRemainAmount(50);
        tx.setExpireTime(now.plusYears(1));
        tx.setDescription(activity.getTitle() + "签到");
        tx.setRefId(activityId);
        pointTransactionMapper.insert(tx);


    }

}
