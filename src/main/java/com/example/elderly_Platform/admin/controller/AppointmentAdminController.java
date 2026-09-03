package com.example.elderly_Platform.admin.controller;

import com.example.elderly_Platform.admin.service.AppointmentAdminService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.entity.AppointmentPackage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


/**
 * 管理端预约管理控制器 —— 管理预约套餐、时段、预约订单及健康报告
 * <p>仅管理员角色可访问（需在请求头携带管理员 Token）</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/appointment")
public class AppointmentAdminController {
    private final AppointmentAdminService appointmentAdminService;

    // ==================== 套餐管理 ====================

    /**
     * 分页查询预约套餐列表
     *
     * @param pageNum        页码，默认 1
     * @param pageSize       每页数量，默认 10
     * @param suitablePeople 适用人群（可选筛选条件）
     * @return 分页的套餐列表
     */
    @GetMapping("/package")
    public Result<?> getAppointmentPackages(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String suitablePeople) {
        return Result.success(appointmentAdminService.getAppointmentPackages(pageNum, pageSize, suitablePeople));
    }

    /**
     * 获取套餐详情
     *
     * @param id 套餐 ID
     * @return 套餐详情
     */
    @GetMapping("/package/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(appointmentAdminService.getDetail(id));
    }

    /**
     * 创建预约套餐
     *
     * @param appointmentPackage 套餐信息（名称、项目、价格、适用人群等）
     * @return 创建成功后返回空数据
     */
    @PostMapping("/package")
    public Result<?> create(@RequestBody AppointmentPackage appointmentPackage) {
        appointmentAdminService.createAppointmentPackage(appointmentPackage);
        return Result.success();
    }

    /**
     * 编辑预约套餐
     *
     * @param id                 套餐 ID
     * @param appointmentPackage 修改后的套餐信息
     * @return 更新成功后返回空数据
     */
    @PutMapping("/package/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody AppointmentPackage appointmentPackage) {
        appointmentAdminService.updateAppointmentPackage(id, appointmentPackage);
        return Result.success();
    }

    /**
     * 删除预约套餐
     *
     * @param id 套餐 ID
     * @return 删除成功后返回空数据
     */
    @DeleteMapping("/package/{id}")
    public Result<?> delete(@PathVariable Long id) {
        appointmentAdminService.deleteAppointmentPackage(id);
        return Result.success();
    }

    // ==================== 时段管理 ====================

    /**
     * 批量生成预约时段
     * <p>为指定套餐在日期范围内，按每天多个时间段批量生成可预约的时段</p>
     *
     * @param packageId  套餐 ID
     * @param startDate  开始日期（yyyy-MM-dd）
     * @param endDate    结束日期（yyyy-MM-dd）
     * @param timeRanges 每天的时间段列表，如：["09:00-10:00", "14:00-15:00"]
     * @param maxCount   每个时段的最大可预约人数
     * @return 生成成功后返回空数据
     */
    @PostMapping("/slot/generate")
    public Result<?> generateSlots(@RequestParam Long packageId,
                                    @RequestParam LocalDate startDate,
                                    @RequestParam LocalDate endDate,
                                    @RequestParam List<String> timeRanges,
                                    @RequestParam Integer maxCount) {
        appointmentAdminService.generateSlots(packageId, startDate, endDate, timeRanges, maxCount);
        return Result.success();
    }

    // ==================== 预约管理 ====================

    /**
     * 分页查询所有预约订单
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @param status   预约状态（可选：待确认/已确认/已完成/已取消）
     * @return 分页的预约订单列表
     */
    @GetMapping
    public Result<?> listAppointments(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String status) {
        return Result.success(appointmentAdminService.listAppointments(pageNum, pageSize, status));
    }

    /**
     * 确认预约订单
     *
     * @param id 预约订单 ID
     * @return 确认成功后返回空数据
     */
    @PutMapping("/{id}/confirm")
    public Result<?> confirmAppointment(@PathVariable Long id) {
        appointmentAdminService.confirmAppointment(id);
        return Result.success();
    }

    /**
     * 取消预约订单
     *
     * @param id 预约订单 ID
     * @return 取消成功后返回空数据
     */
    @PutMapping("/{id}/cancel")
    public Result<?> cancelAppointment(@PathVariable Long id) {
        appointmentAdminService.cancelAppointment(id);
        return Result.success();
    }

    // ==================== 报告上传 ====================

    /**
     * 为预约上传健康报告
     * <p>预约完成后，管理员上传体检报告/健康报告的文件地址</p>
     *
     * @param id        预约订单 ID
     * @param reportUrl 报告文件 URL 地址
     * @param adminId   上传报告的管理员 ID
     * @return 上传成功后返回空数据
     */
    @PostMapping("/{id}/report")
    public Result<?> uploadReport(@PathVariable Long id,
                                   @RequestParam String reportUrl,
                                   @RequestParam Long adminId) {
        appointmentAdminService.uploadReport(id, reportUrl, adminId);
        return Result.success();
    }
}
