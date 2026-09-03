package com.example.elderly_Platform.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityJoinDTO {
    @NotBlank(message = "活动ID不可为空")
    private Long activityId;
}
