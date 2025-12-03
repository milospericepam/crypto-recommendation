package com.example.cryptorecommendations.service;

import com.example.dto.CryptoNormalizedRangeDto;
import com.example.dto.CryptoStatsDto;
import com.example.dto.HighestNormalizedRangeDto;
import com.example.exception.CryptoNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CryptoServiceIntegrationTest {

    private final CryptoService cryptoService = new CryptoService();

    @Test
    void getCryptosSortedByNormalizedRange_returnsNonEmptyList() {
        List<CryptoNormalizedRangeDto> result = cryptoService.getCryptosSortedByNormalizedRange();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getSymbol());
    }

    @Test
    void getCryptoStats_returnsStatsForValidSymbol() {
        CryptoStatsDto stats = cryptoService.getCryptoStats("BTC");
        assertEquals("BTC", stats.getSymbol());
        assertTrue(stats.getMinPrice() <= stats.getMaxPrice());
    }

    @Test
    void getCryptoStats_throwsForInvalidSymbol() {
        assertThrows(CryptoNotFoundException.class, () -> cryptoService.getCryptoStats("FAKECOIN"));
    }

    @Test
    void getCryptoStatsForPeriod_returnsStatsForValidPeriod() {
        CryptoStatsDto stats = cryptoService.getCryptoStatsForPeriod("BTC", 10, ChronoUnit.YEARS);
        assertEquals("BTC", stats.getSymbol());
        assertTrue(stats.getMinPrice() <= stats.getMaxPrice());
    }

    @Test
    void getCryptoStatsForPeriod_throwsForNoData() {
        assertThrows(CryptoNotFoundException.class, () -> cryptoService.getCryptoStatsForPeriod("BTC", 1, ChronoUnit.YEARS));
    }

    @Test
    void getCryptoWithHighestNormalizedRange_returnsResultForValidDate() {
        HighestNormalizedRangeDto result = cryptoService.getCryptoWithHighestNormalizedRange("2022-01-03");
        assertNotNull(result);
        assertNotNull(result.getSymbol());
        assertEquals("2022-01-03", result.getDate());
    }

    @Test
    void getCryptoWithHighestNormalizedRange_returnsNullForNoData() {
        HighestNormalizedRangeDto result = cryptoService.getCryptoWithHighestNormalizedRange("1900-01-01");
        assertNull(result);
    }
}