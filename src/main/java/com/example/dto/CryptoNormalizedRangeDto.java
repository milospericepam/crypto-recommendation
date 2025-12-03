package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CryptoNormalizedRangeDto {
    private String symbol;
    private double normalizedRange;
}
