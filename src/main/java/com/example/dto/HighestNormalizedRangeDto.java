package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HighestNormalizedRangeDto {
    private String symbol;
    private double normalizedRange;
    private String date;
}