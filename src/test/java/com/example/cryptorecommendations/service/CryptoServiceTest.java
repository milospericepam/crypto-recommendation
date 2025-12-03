package com.example.cryptorecommendations.service;

import com.example.dto.CryptoNormalizedRangeDto;
import com.example.dto.CryptoStatsDto;
import com.example.dto.HighestNormalizedRangeDto;
import com.example.exception.CryptoIsMissingException;
import com.example.exception.CryptoNotFoundException;
import com.example.model.CryptoPriceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = Mockito.spy(new CryptoService());
    }

    @Test
    void testGetCryptoStats_returnsCorrectStats() {
        String symbol = "BTC";
        List<CryptoPriceEntry> mockEntries = List.of(
                new CryptoPriceEntry(symbol, LocalDateTime.now().minusDays(2), 100.0),
                new CryptoPriceEntry(symbol, LocalDateTime.now().minusDays(1), 200.0),
                new CryptoPriceEntry(symbol, LocalDateTime.now(), 150.0)
        );
        doReturn(List.of(symbol)).when(cryptoService).getAvailableSymbols();
        doReturn(mockEntries).when(cryptoService).getPriceDataFromCsvFile(symbol);

        CryptoStatsDto stats = cryptoService.getCryptoStats(symbol);

        assertEquals(symbol, stats.getSymbol());
        assertEquals(100.0, stats.getOldestPrice());
        assertEquals(150.0, stats.getNewestPrice());
        assertEquals(100.0, stats.getMinPrice());
        assertEquals(200.0, stats.getMaxPrice());
    }

    @Test
    void testGetCryptoStats_throwsNotFoundForUnknownSymbol() {
        doReturn(List.of("BTC")).when(cryptoService).getAvailableSymbols();
        assertThrows(CryptoNotFoundException.class, () -> cryptoService.getCryptoStats("ETH"));
    }

    @Test
    void testGetCryptoStats_throwsMissingForNoData() {
        String symbol = "BTC";
        doReturn(List.of(symbol)).when(cryptoService).getAvailableSymbols();
        doReturn(List.of()).when(cryptoService).getPriceDataFromCsvFile(symbol);
        assertThrows(CryptoIsMissingException.class, () -> cryptoService.getCryptoStats(symbol));
    }

    @Test
    void testGetCryptoStatsForPeriod_returnsCorrectStats() {
        String symbol = "BTC";
        LocalDateTime now = LocalDateTime.now();
        List<CryptoPriceEntry> mockEntries = List.of(
                new CryptoPriceEntry(symbol, now.minusDays(40), 100.0),
                new CryptoPriceEntry(symbol, now.minusDays(10), 200.0),
                new CryptoPriceEntry(symbol, now.minusDays(5), 150.0)
        );
        doReturn(mockEntries).when(cryptoService).getPriceDataFromCsvFile(symbol);

        CryptoStatsDto stats = cryptoService.getCryptoStatsForPeriod(symbol, 30, ChronoUnit.DAYS);

        assertEquals(symbol, stats.getSymbol());
        assertEquals(200.0, stats.getOldestPrice());
        assertEquals(150.0, stats.getNewestPrice());
        assertEquals(150.0, stats.getMinPrice());
        assertEquals(200.0, stats.getMaxPrice());
    }

    @Test
    void testGetCryptosSortedByNormalizedRange_returnsSortedList() {
        List<CryptoPriceEntry> mockEntries = List.of(
                new CryptoPriceEntry("BTC", LocalDateTime.now(), 100.0),
                new CryptoPriceEntry("BTC", LocalDateTime.now(), 200.0),
                new CryptoPriceEntry("ETH", LocalDateTime.now(), 50.0),
                new CryptoPriceEntry("ETH", LocalDateTime.now(), 150.0)
        );
        doReturn(mockEntries).when(cryptoService).getPriceDataFromAllCsvFiles();

        List<CryptoNormalizedRangeDto> result = cryptoService.getCryptosSortedByNormalizedRange();

        assertEquals(2, result.size());
        assertEquals("ETH", result.get(0).getSymbol()); // ETH has higher normalized range
        assertEquals("BTC", result.get(1).getSymbol());
    }

    @Test
    void testGetCryptoWithHighestNormalizedRange_returnsCorrectCrypto() {
        String symbol = "BTC";
        LocalDateTime now = LocalDateTime.now();
        List<String> symbols = List.of(symbol);
        doReturn(symbols).when(cryptoService).getAvailableSymbols();
        doReturn(List.of(
                new CryptoPriceEntry(symbol, now, 100.0),
                new CryptoPriceEntry(symbol, now, 200.0)
        )).when(cryptoService).getPriceDataFromCsvFile(symbol);

        HighestNormalizedRangeDto result = cryptoService.getCryptoWithHighestNormalizedRange(now.toLocalDate().toString());

        assertNotNull(result);
        assertEquals(symbol, result.getSymbol());
        assertEquals((200.0 - 100.0) / 100.0, result.getNormalizedRange());
    }
}
