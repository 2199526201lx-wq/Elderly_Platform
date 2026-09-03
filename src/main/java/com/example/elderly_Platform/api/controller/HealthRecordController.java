package com.example.elderly_Platform.api.controller;

import com.example.elderly_Platform.api.service.HealthRecordService;
import com.example.elderly_Platform.core.common.Result;
import com.example.elderly_Platform.core.dto.HealthRecordDTO;
import com.example.elderly_Platform.core.entity.HealthRecord;
import com.example.elderly_Platform.core.vo.HealthRecordVO;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 健康档案控制器 —— 处理会员健康数据录入、历史查询、趋势分析等功能
 * <p>需会员登录后访问（需在请求头携带会员 Token）</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member/healthrecord")
public class HealthRecordController {
    private final HealthRecordService healthRecordService;

    /**
     * 健康数据录入
     */
    @PostMapping("/record")
    public Result<?> record(@Valid @RequestBody HealthRecordDTO dto){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        healthRecordService.record(userId, dto);
        return Result.success();
    }

    /**
     * 健康记录历史分页查询
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageInfo<HealthRecord> page = healthRecordService.list(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 健康趋势分析
     */
    @GetMapping("/analyze")
    public Result<?> analyze(@RequestParam(defaultValue = "6")Integer months){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HealthRecordVO vo=healthRecordService.analyze(userId,months);
        return Result.success(vo);
    }
}
