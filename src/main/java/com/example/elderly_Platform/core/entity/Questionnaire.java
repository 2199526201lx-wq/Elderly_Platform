package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Questionnaire {
    private Long id;
    private String title;
    private String description;
    private String status;
    private Integer totalScore;
    private Integer passScore;
    private String gradeRules;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
