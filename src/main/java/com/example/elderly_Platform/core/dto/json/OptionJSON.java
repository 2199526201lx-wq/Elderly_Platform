package com.example.elderly_Platform.core.dto.json;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OptionJSON {
    private String text;
    private BigDecimal score;
    private String meaning;
}
