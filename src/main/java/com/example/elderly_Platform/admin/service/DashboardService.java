package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.mapper.ActivityRegistrationMapper;
import com.example.elderly_Platform.core.mapper.AppointmentMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.example.elderly_Platform.core.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理端仪表盘服务 —— 提供仪表盘汇总数据（会员统计、预约统计等）
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserMapper userMapper;
    private final AppointmentMapper appointmentMapper;
    private final ActivityRegistrationMapper registrationMapper;

    /**
     * 获取仪表盘汇总数据
     *
     * @return 包含会员总数、今日新增会员数、今日预约数、今日报名数及待确认预约数的 DashboardVO
     */
    public DashboardVO getSummary() {
        DashboardVO vo = new DashboardVO();
        vo.setMemberTotal(userMapper.countMembers());
        vo.setMemberToday(userMapper.countMembersToday());
        vo.setAppointmentToday(appointmentMapper.countToday());
        vo.setRegistrationToday(registrationMapper.countToday());
        vo.setAppointmentPending(appointmentMapper.countPending());
        return vo;
    }
}