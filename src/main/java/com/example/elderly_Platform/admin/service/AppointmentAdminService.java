package com.example.elderly_Platform.admin.service;

import com.example.elderly_Platform.core.entity.Appointment;
import com.example.elderly_Platform.core.entity.AppointmentPackage;
import com.example.elderly_Platform.core.entity.AppointmentSlot;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.AppointmentMapper;
import com.example.elderly_Platform.core.mapper.AppointmentPackageMapper;
import com.example.elderly_Platform.core.mapper.AppointmentSlotMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端预约服务 —— 处理预约套餐、预约时段、预约记录及报告上传等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AppointmentAdminService {
    private final AppointmentPackageMapper appointmentPackageMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final AppointmentMapper appointmentMapper;

    /**
     * 分页查询预约套餐列表（支持按适用人群筛选）
     *
     * @param pageNum       页码
     * @param pageSize      每页数量
     * @param suitablePeople 适用人群（可选，如：老年人、残疾人等）
     * @return 分页的预约套餐列表 PageInfo
     */
    public PageInfo<AppointmentPackage> getAppointmentPackages(Integer pageNum, Integer pageSize,String suitablePeople) {
        PageHelper.startPage(pageNum,pageSize);
        AppointmentPackage appointmentPackage = new AppointmentPackage();
        appointmentPackage.setSuitablePeople(suitablePeople);
        List<AppointmentPackage> list= appointmentPackageMapper.selectByCondition(appointmentPackage);
        return new PageInfo<>(list);
    }

    /**
     * 根据套餐ID查询预约套餐详情
     *
     * @param appointmentPackageId 预约套餐ID
     * @return 预约套餐实体
     * @throws BusinessException 当套餐不存在时抛出异常
     */
    public AppointmentPackage getDetail(Long appointmentPackageId){
        AppointmentPackage appointmentPackage = appointmentPackageMapper.selectById(appointmentPackageId);
        if(appointmentPackage == null){
            throw new BusinessException("套餐不存在");
        }
        return appointmentPackage;
    }

    /**
     * 创建预约套餐，创建后套餐状态默认为"启用"
     *
     * @param appointmentPackage 预约套餐实体
     */
    public void createAppointmentPackage(AppointmentPackage appointmentPackage){
        appointmentPackage.setStatus("启用");
        appointmentPackageMapper.insert(appointmentPackage);
    }
    /**
     * 更新预约套餐信息
     *
     * @param appointmentAdminId 预约套餐ID
     * @param appointmentPackage 预约套餐实体（包含需要更新的字段）
     * @throws BusinessException 当套餐不存在时抛出异常
     */
    public void updateAppointmentPackage(Long appointmentAdminId,AppointmentPackage appointmentPackage){
        if(appointmentPackageMapper.selectById(appointmentAdminId) == null){
            throw new BusinessException("套餐不存在");
        }
        appointmentPackageMapper.updateById(appointmentPackage);
    }
    /**
     * 删除预约套餐（逻辑删除）
     *
     * @param appointmentAdminId 预约套餐ID
     * @throws BusinessException 当套餐不存在时抛出异常
     */
    public void deleteAppointmentPackage(Long appointmentAdminId){
        AppointmentPackage appointmentPackage = appointmentPackageMapper.selectById(appointmentAdminId);
        if(appointmentPackage == null){
            throw new BusinessException("套餐不存在");
        }
        appointmentPackageMapper.deleteById(appointmentAdminId);
    }

    /**
     * 批量生成预约时段
     * <p>在指定日期范围内，为每个日期遍历每个时间段生成预约时段，初始状态为"可预约"</p>
     *
     * @param packageId  预约套餐ID
     * @param startDate  开始日期（含）
     * @param endDate    结束日期（含）
     * @param timeRanges 时间段列表（如：["09:00-10:00", "10:00-11:00"]）
     * @param maxCount   每个时段的最大可预约人数
     * @throws BusinessException 当套餐不存在、日期范围非法或时间段列表为空时抛出异常
     */
    // ==================== 批量生成预约时段 ====================
    public void generateSlots(Long packageId, LocalDate startDate, LocalDate endDate,
                              List<String> timeRanges, Integer maxCount) {
        // 校验套餐是否存在
        if (appointmentPackageMapper.selectById(packageId) == null) {
            throw new BusinessException("套餐不存在");
        }
        // 校验日期
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        // 校验时间段
        if (timeRanges == null || timeRanges.isEmpty()) {
            throw new BusinessException("时间段列表不能为空");
        }

        // 遍历每一天
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 遍历每个时间段
            for (String timeRange : timeRanges) {
                AppointmentSlot slot = new AppointmentSlot();
                slot.setPackageId(packageId);
                slot.setAppointDate(date);
                slot.setTimeRange(timeRange);
                slot.setMaxCount(maxCount);
                slot.setCurrentCount(0);
                slot.setStatus("可预约");
                appointmentSlotMapper.insert(slot);
            }
        }
    }

    // ==================== 预约管理 ====================

    /**
     * 分页查询预约记录列表（支持按状态筛选）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   预约状态（可选，如：待确认、已确认、已取消、已完成）
     * @return 分页的预约记录列表 PageInfo
     */
    public PageInfo<Appointment> listAppointments(Integer pageNum, Integer pageSize, String status) {
        PageHelper.startPage(pageNum, pageSize);
        Appointment condition = new Appointment();
        condition.setStatus(status);
        List<Appointment> list = appointmentMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    /**
     * 确认预约
     * <p>只有状态为"待确认"的预约才能被确认</p>
     *
     * @param id 预约ID
     * @throws BusinessException 当预约不存在或状态不为"待确认"时抛出异常
     */
    public void confirmAppointment(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if (!"待确认".equals(appointment.getStatus())) {
            throw new BusinessException("只有待确认的预约才能确认");
        }
        appointment.setStatus("已确认");
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
    }

    /**
     * 取消预约
     * <p>取消预约后会自动退还对应时段的预约名额</p>
     *
     * @param id 预约ID
     * @throws BusinessException 当预约不存在或状态为"已完成"/"已取消"时抛出异常
     */
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if ("已完成".equals(appointment.getStatus()) || "已取消".equals(appointment.getStatus())) {
            throw new BusinessException("该预约无法取消");
        }
        appointment.setStatus("已取消");
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        // 退还时段名额
        appointmentSlotMapper.decrementCount(appointment.getSlotId());
    }

    // ==================== 报告上传 ====================

    /**
     * 上传体检报告并完成预约
     * <p>上传报告后，预约状态变更为"已完成"</p>
     *
     * @param id         预约ID
     * @param reportUrl  报告文件的访问地址
     * @param adminId    上传报告的管理员ID
     * @throws BusinessException 当预约不存在时抛出异常
     */
    public void uploadReport(Long id, String reportUrl, Long adminId) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        appointment.setReportUrl(reportUrl);
        appointment.setReportUploadTime(LocalDateTime.now());
        appointment.setUploadAdminId(adminId);
        appointment.setStatus("已完成");
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
    }

}
