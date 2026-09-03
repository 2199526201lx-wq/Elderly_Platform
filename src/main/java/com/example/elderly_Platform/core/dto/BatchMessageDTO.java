package com.example.elderly_Platform.core.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchMessageDTO {
    private List<Long> userIds;
    private String title;
    private String content;
    private String type;
}
