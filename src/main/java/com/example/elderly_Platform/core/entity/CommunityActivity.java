package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityActivity {
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
