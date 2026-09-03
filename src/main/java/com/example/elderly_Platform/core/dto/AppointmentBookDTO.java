package com.example.elderly_Platform.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentBookDTO {
    @NotNull(message = "时段id不能为空")
    private Long slotId;
}
