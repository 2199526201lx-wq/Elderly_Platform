package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssessmentResult {
    private Long id;
    private Long userId;
    private Long questionnaireId;
    private String answers;
    private Integer ruleScore;
    private Integer aiScore;
    private String aiSuggestion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
