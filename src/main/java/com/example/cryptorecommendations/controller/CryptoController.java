package com.example.cryptorecommendations.controller;

import com.example.cryptorecommendations.service.CryptoService;
import com.example.dto.CryptoNormalizedRangeDto;
import com.example.dto.CryptoStatsDto;
import com.example.dto.HighestNormalizedRangeDto;
import com.example.exception.CryptoIsMissingException;
import com.example.exception.CryptoNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/cryptos")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Operation(summary = "Get cryptos sorted by normalized range")
    @GetMapping("/normalized-range")
    public List<CryptoNormalizedRangeDto> getCryptosSortedByNormalizedRange() {
        return cryptoService.getCryptosSortedByNormalizedRange();
    }

    @Operation(summary = "Get statistic for a specific crypto")
    @GetMapping("/{cryptoSymbol}/stats")
    public CryptoStatsDto getCryptoStats(
            @Parameter(description = "Crypto symbol in upper case")
            @PathVariable String cryptoSymbol) {
        return cryptoService.getCryptoStats(cryptoSymbol);
    }

    @Operation(summary = "Get statistic for a specific crypto for the last month")
    @GetMapping("/{symbol}/stats/period")
    public CryptoStatsDto getCryptoStatsForPeriod(
            @PathVariable String symbol,
            @RequestParam int period,
            @RequestParam ChronoUnit periodType) {
        return cryptoService.getCryptoStatsForPeriod(symbol, period, periodType);
    }

    @Operation(summary = "Get crypto with highest normalized range for a specific day")
    @GetMapping("/highest-normalized-range")
    public HighestNormalizedRangeDto getCryptoWithHighestNormalizedRange(
            @Parameter(description = "Date in the YYYY-MM-DD format")
            @RequestParam String date) {
        return cryptoService.getCryptoWithHighestNormalizedRange(date);
    }

    @ExceptionHandler(CryptoNotFoundException.class)
    public ResponseEntity<String> handleCryptoNotFound(CryptoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CryptoIsMissingException.class)
    public ResponseEntity<String> handleCryptoNotFound(CryptoIsMissingException ex) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ex.getMessage());
    }


}
