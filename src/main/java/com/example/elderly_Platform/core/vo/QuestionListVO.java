package com.example.elderly_Platform.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionListVO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createTime;
}
