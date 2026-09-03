package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.entity.Appointment;
import com.example.elderly_Platform.core.entity.AppointmentPackage;
import com.example.elderly_Platform.core.entity.AppointmentSlot;
import com.example.elderly_Platform.core.entity.User;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.AppointmentMapper;
import com.example.elderly_Platform.core.mapper.AppointmentPackageMapper;
import com.example.elderly_Platform.core.mapper.AppointmentSlotMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约服务 —— 处理套餐浏览、时段查询、预约下单、取消预约等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentMapper appointmentMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final AppointmentPackageMapper appointmentPackageMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询套餐列表（仅展示启用的套餐）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页的启用套餐列表 PageInfo
     */
    public PageInfo<AppointmentPackage> listPackages(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        AppointmentPackage condition = new AppointmentPackage();
        condition.setStatus("启用");
        return new PageInfo<>(appointmentPackageMapper.selectByCondition(condition));
    }

    /**
     * 查询某套餐在某日期的可预约时段
     *
     * @param packageId 套餐ID
     * @param date      查询日期
     * @return 可预约时段列表
     */
    public List<AppointmentSlot> listSlots(Long packageId, LocalDate date) {
        AppointmentSlot condition = new AppointmentSlot();
        condition.setPackageId(packageId);
        condition.setAppointDate(date);
        condition.setStatus("可预约");
        return appointmentSlotMapper.selectByCondition(condition);
    }

    /**
     * 分页查询我的预约记录（可按状态筛选）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   预约状态（可选）
     * @return 分页的预约记录 PageInfo
     */
    public PageInfo<Map<String, Object>> myList(Long userId, Integer pageNum, Integer pageSize, String status) {
        PageHelper.startPage(pageNum, pageSize);
        Appointment condition = new Appointment();
        condition.setUserId(userId);
        if (status != null && !status.isEmpty()) {
            condition.setStatus(status);
        }
        PageInfo<Appointment> pageInfo = new PageInfo<>(appointmentMapper.selectByCondition(condition));
        // 为每条预约附带套餐名称，前端「我的预约」页面需要展示 item.packageName
        List<Map<String, Object>> rows = pageInfo.getList().stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("userId", a.getUserId());
            m.put("slotId", a.getSlotId());
            m.put("packageId", a.getPackageId());
            m.put("status", a.getStatus());
            m.put("reportUrl", a.getReportUrl());
            m.put("createTime", a.getCreateTime());
            AppointmentPackage pkg = appointmentPackageMapper.selectById(a.getPackageId());
            m.put("packageName", pkg != null ? pkg.getName() : null);
            return m;
        }).collect(Collectors.toList());
        PageInfo<Map<String, Object>> result = new PageInfo<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setList(rows);
        return result;
    }

    /**
     * 预约下单
     * <p>校验用户状态、时段可用性及积分余额，预约成功后扣除积分并增加时段已预约人数</p>
     *
     * @param userId 用户ID
     * @param slotId 时段ID
     * @throws BusinessException 当用户被禁用、时段不存在、名额已满或积分不足时抛出异常
     */
    @Transactional
    public void book(Long userId, Long slotId) {
        User user = userMapper.selectById(userId);
        if ("禁用".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        AppointmentSlot slot = appointmentSlotMapper.selectForUpdate(slotId);
        if (slot == null) {
            throw new BusinessException("时段不存在");
        }
        if (slot.getCurrentCount() >= slot.getMaxCount()) {
            throw new BusinessException("该时段名额已满");
        }

        int price = appointmentSlotMapper.selectPackagePrice(slotId);
        if (user.getPoints() < price) {
            throw new BusinessException("积分不足，无法预约");
        }

        userMapper.deductPoints(userId, price);
        appointmentSlotMapper.incrementCount(slotId);
        Appointment at = new Appointment();
        at.setUserId(userId);
        at.setSlotId(slotId);
        at.setPackageId(slot.getPackageId());
        at.setStatus("待确认");
        appointmentMapper.insert(at);
    }

    /**
     * 取消预约
     * <p>校验预约归属与状态，取消后释放时段名额并退还积分</p>
     *
     * @param userId        用户ID
     * @param appointmentId 预约记录ID
     * @throws BusinessException 当预约不存在、不属于该用户或状态不可取消时抛出异常
     */
    @Transactional
    public void cancel(Long userId, Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            throw new BusinessException("预约不存在");
        }
        if (!"待确认".equals(appointment.getStatus()) && !"已确认".equals(appointment.getStatus())) {
            throw new BusinessException("当前状态不可取消");
        }
        appointmentSlotMapper.decrementCount(appointment.getSlotId());
        appointment.setStatus("已取消");
        appointmentMapper.updateById(appointment);

        int price = appointmentSlotMapper.selectPackagePrice(appointment.getSlotId());
        userMapper.addPoints(userId, price);
    }
}
