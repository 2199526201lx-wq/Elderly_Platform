package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.AppointmentService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.AppointmentBookDTO;
import com.example.elderly_Platform.core.entity.Appointment;
import com.example.elderly_Platform.core.entity.AppointmentPackage;
import com.example.elderly_Platform.core.entity.AppointmentSlot;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 会员预约控制器 —— 处理会员浏览套餐、预约时段、提交预约、取消预约等功能
 * <p>需会员登录后访问（需在请求头携带会员 Token）</p>
 */
@RestController
@RequestMapping("/api/member/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    /**
     * 套餐列表查询（分页）
     */
    @GetMapping("/packages")
    public Result<?> listPackages(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<AppointmentPackage> page = appointmentService.listPackages(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 查询某套餐某日期的可预约时段
     */
    @GetMapping("/slots")
    public Result<?> listSlots(@RequestParam Long packageId,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){
        List<AppointmentSlot> slots = appointmentService.listSlots(packageId, date);
        return Result.success(slots);
    }

    /**
     * 我的预约记录（分页）
     */
    @GetMapping("/list")
    public Result<?> myList(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) String status){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageInfo<Map<String,Object>> page = appointmentService.myList(userId, pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * 预约提交
     */
    @PostMapping("/book")
    public Result<?> book(@RequestBody @Valid AppointmentBookDTO dto){
        Long userId=(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        appointmentService.book(userId,dto.getSlotId());
        return Result.success();
    }

    /**
     * 预约取消
     */
    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id){
        Long userId=(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        appointmentService.cancel(userId,id);
        return Result.success();
    }
}
