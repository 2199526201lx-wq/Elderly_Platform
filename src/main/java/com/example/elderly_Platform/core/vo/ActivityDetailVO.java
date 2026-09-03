package com.example.elderly_Platform.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityDetailVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String content;
    private String location;
    private LocalDateTime registrationStartTime;
    private LocalDateTime registrationEndTime;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status;
    // 是否已报名
    private Boolean joined;
    // 是否已签到
    private Boolean checkedIn;
}

