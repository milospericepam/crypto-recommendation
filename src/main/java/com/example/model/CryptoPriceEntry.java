package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Getter
public class CryptoPriceEntry {
    private String symbol;
    private LocalDateTime dateTime;
    private double price;
}