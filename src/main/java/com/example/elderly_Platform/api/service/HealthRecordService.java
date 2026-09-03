package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.dto.HealthRecordDTO;
import com.example.elderly_Platform.core.entity.HealthGuidance;
import com.example.elderly_Platform.core.entity.HealthRecord;
import com.example.elderly_Platform.core.entity.Message;
import com.example.elderly_Platform.core.entity.User;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.HealthGuidanceMapper;
import com.example.elderly_Platform.core.mapper.HealthRecordMapper;
import com.example.elderly_Platform.core.mapper.MessageMapper;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.example.elderly_Platform.core.vo.HealthRecordVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 健康记录服务 —— 管理用户的健康指标记录、历史查询与统计分析。
 * <p>
 * 每次记录健康指标时，若用户已设置身高则自动计算 BMI，
 * 并对收缩压、舒张压、空腹血糖、心率、BMI 五项指标进行异常检测，
 * 发现异常时自动生成健康指导与站内消息提醒。
 */
@Service
@RequiredArgsConstructor
public class HealthRecordService {
    private final UserMapper userMapper;
    private final HealthGuidanceMapper healthGuidanceMapper;
    private final MessageMapper messageMapper;
    private final HealthRecordMapper healthRecordMapper;

    /**
     * 记录用户的健康指标并自动检测异常
     * <p>
     * 保存健康记录（收缩压、舒张压、血糖、心率、体重、BMI 等），
     * 然后对五项核心指标进行范围检测。若某指标超出正常范围且当天尚未提醒过，
     * 则自动生成一条健康指导（类型为"数据小结"）和一条站内消息提醒。
     * 该方法在事务中执行，任一环节失败会整体回滚。
     *
     * @param userId 当前登录用户 ID
     * @param dto    健康记录数据传输对象，包含各项健康指标数据
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional
    public void record(Long userId, HealthRecordDTO dto) {
        User user=userMapper.selectById(userId);
        if(user==null){
            throw new BusinessException("用户不存在");
        }

        BigDecimal bmi = null;  // 默认 null
        if (user.getHeight() != null) {
            // 有身高才计算 BMI
            BigDecimal height = user.getHeight().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal heightSquared = height.multiply(height);
            bmi = dto.getWeight().divide(heightSquared, 2, RoundingMode.HALF_UP);
        }

        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setSystolic(dto.getSystolic());
        record.setDiastolic(dto.getDiastolic());
        record.setBloodSugar(dto.getBloodSugar());
        record.setHeartRate(dto.getHeartRate());
        record.setWeight(dto.getWeight());
        record.setBmi(bmi);
        record.setRecordedTime(dto.getRecordedTime() != null ? dto.getRecordedTime() : LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        healthRecordMapper.insertHealthyRecord(record);

        List<IndicatorRule> rules=new ArrayList<>();
        rules.add(new IndicatorRule("收缩压", "systolic",
                BigDecimal.valueOf(dto.getSystolic()),
                BigDecimal.valueOf(90), BigDecimal.valueOf(139), "mmHg"));
        rules.add(new IndicatorRule("舒张压", "diastolic",
                BigDecimal.valueOf(dto.getDiastolic()),
                BigDecimal.valueOf(60), BigDecimal.valueOf(89), "mmHg"));
        rules.add(new IndicatorRule("空腹血糖", "bloodSugar",
                dto.getBloodSugar(),
                BigDecimal.valueOf(3.9), BigDecimal.valueOf(6.1), "mmol/L"));
        rules.add(new IndicatorRule("心率", "heartRate",
                BigDecimal.valueOf(dto.getHeartRate()),
                BigDecimal.valueOf(60), BigDecimal.valueOf(100), "次/分"));
        rules.add(new IndicatorRule("BMI", "bmi",
                bmi,
                BigDecimal.valueOf(18.5), BigDecimal.valueOf(23.9), ""));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();          // 今天 00:00
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay(); // 明天 00:00

        for (IndicatorRule rule : rules) {
            if (rule.value == null) {
                continue;   // 跳过这条，不算、不提醒
            }
            if(rule.value.compareTo(rule.max)>0 ||rule.value.compareTo(rule.min)<0){
                int count=healthGuidanceMapper.selectRule(userId,rule.indicator,startOfDay,endOfDay);
                if(count==0){
                String s="您的"+rule.name+"（"+rule.value+"）不正常，"+"正常范围是（"+rule.min+"-"+rule.max+"），请注意安全";

                HealthGuidance hg=new HealthGuidance();
                hg.setUserId(userId);
                hg.setIndicator(rule.indicator);
                hg.setContent(s);
                hg.setType("数据小结");
                hg.setIsRead(0);
                hg.setCreateTime(LocalDateTime.now());
                hg.setUpdateTime(LocalDateTime.now());
                healthGuidanceMapper.insert(hg);

                Message message=new Message();
                message.setUserId(userId);
                message.setType("健康提醒");
                message.setContent(s);
                message.setTitle("健康提醒");
                message.setIsRead(0);
                message.setCreateTime(LocalDateTime.now());
                message.setUpdateTime(LocalDateTime.now());
                messageMapper.insert(message);
            }
            }
        }
    }
    private record IndicatorRule(
            String name,
            String indicator,
            BigDecimal value,
            BigDecimal min,
            BigDecimal max,
            String unit
    ){}

    /**
     * 分页查询用户的健康记录历史
     *
     * @param userId   当前登录用户 ID
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果对象，包含健康记录列表及分页信息
     */
    public PageInfo<HealthRecord> list(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        HealthRecord condition = new HealthRecord();
        condition.setUserId(userId);
        return new PageInfo<>(healthRecordMapper.selectByCondition(condition));
    }

    /**
     * 统计分析用户最近一段时间的健康指标
     * <p>
     * 统计从当前日期往前推指定月份数的所有健康记录，
     * 返回各指标的统计汇总数据（如均值、最值等）。
     *
     * @param userId 当前登录用户 ID
     * @param months 统计的时间跨度（月数）
     * @return 健康记录统计视图对象，包含各指标统计结果
     */
    public HealthRecordVO analyze(Long userId,Integer months){
        LocalDateTime startTime =LocalDate.now().minusMonths(months).atStartOfDay();
        return healthRecordMapper.selectStats(userId,startTime);
    }
}
