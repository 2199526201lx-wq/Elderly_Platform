package com.example.elderly_Platform.core.vo;

import lombok.Data;

@Data
public class DashboardVO {
    private Integer memberTotal;        // 会员总数
    private Integer memberToday;        // 今日新增会员
    private Integer appointmentToday;   // 今日预约数
    private Integer registrationToday;  // 今日活动报名数
    private Integer appointmentPending; // 待完成预约数
}