package com.example.elderly_Platform.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointTransaction {
    private Long id;
    private Long userId;
    private String type;
    private Integer changeAmount;
    private Integer balanceAfter;
    private Integer remainAmount;
    private LocalDateTime expireTime;
    private Long batchTxId;
    private String description;
    private Long refId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
