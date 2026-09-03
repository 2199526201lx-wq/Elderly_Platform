package com.example.elderly_Platform.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageDTO {
    @NotNull(message="会话id不能为空")
    private Long sessionId;
    @NotNull(message="会话内容不能为空")
    private String content;
}
