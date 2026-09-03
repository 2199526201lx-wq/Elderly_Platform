package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Question {
    private Long id;
    private Long questionnaireId;
    private String content;
    private String type;
    private String options;
    private String scoreMode;
    private Integer maxScore;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
