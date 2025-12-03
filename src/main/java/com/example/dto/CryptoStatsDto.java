package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CryptoStatsDto {
    private String symbol;
    private double oldestPrice;
    private double newestPrice;
    private double minPrice;
    private double maxPrice;
}
