package com.example.cryptorecommendations.service;

import com.example.dto.CryptoNormalizedRangeDto;
import com.example.dto.CryptoStatsDto;
import com.example.dto.HighestNormalizedRangeDto;
import com.example.exception.CryptoIsMissingException;
import com.example.exception.CryptoNotFoundException;
import com.example.model.CryptoPriceEntry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    public List<CryptoNormalizedRangeDto> getCryptosSortedByNormalizedRange() {
        List<CryptoPriceEntry> priceEntries = getPriceDataFromAllCsvFiles();

        Map<String, List<CryptoPriceEntry>> grouped = priceEntries.stream()
                .collect(Collectors.groupingBy(CryptoPriceEntry::getSymbol));

        // Calculate normalized range for each crypto
        List<CryptoNormalizedRangeDto> result = new ArrayList<>();
        for (Map.Entry<String, List<CryptoPriceEntry>> entry : grouped.entrySet()) {
            String symbol = entry.getKey();
            List<CryptoPriceEntry> prices = entry.getValue();
            double min = getMinPrice(prices);
            double max = getMaxPrice(prices);
            double normalizedRange = min > 0 ? (max - min) / min : 0;
            result.add(new CryptoNormalizedRangeDto(symbol, normalizedRange));
        }

        // Sort descending by normalized range
        result.sort(Comparator.comparingDouble(CryptoNormalizedRangeDto::getNormalizedRange).reversed());

        return result;
    }

    public CryptoStatsDto getCryptoStats(String symbol) {
        if (!getAvailableSymbols().contains(symbol.toUpperCase())) {
            throw new CryptoNotFoundException("Crypto '" + symbol + "' doesn't exist.");
        }

        List<CryptoPriceEntry> symbolEntries = getPriceDataFromCsvFile(symbol);

        if (symbolEntries.isEmpty()) {
            throw new CryptoIsMissingException("There are no stats for this crypto");
        }

        CryptoPriceEntry oldest = getOldest(symbolEntries);
        CryptoPriceEntry newest = getNewest(symbolEntries);
        double minPrice = getMinPrice(symbolEntries);
        double maxPrice = getMaxPrice(symbolEntries);

        return new CryptoStatsDto(
                symbol,
                oldest.getPrice(),
                newest.getPrice(),
                minPrice,
                maxPrice
        );
    }

    public CryptoStatsDto getCryptoStatsForPeriod(String symbol, int period, ChronoUnit periodType) {
        List<CryptoPriceEntry> symbolEntries = getPriceDataFromCsvFile(symbol);

        if (symbolEntries.isEmpty()) {
            throw new CryptoIsMissingException("Crypto symbol '" + symbol + "' does not exist or has no data.");
        }

        // Calculate the period
        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(period, periodType);

        // Filter entries for the last month
        List<CryptoPriceEntry> lastMonthEntries = symbolEntries.stream()
                .filter(entry -> entry.getDateTime().isAfter(oneMonthAgo))
                .toList();

        if (lastMonthEntries.isEmpty()) {
            throw new CryptoNotFoundException("No data for '" + symbol + "' in the last month.");
        }

        CryptoPriceEntry oldest = getOldest(lastMonthEntries);
        CryptoPriceEntry newest = getNewest(lastMonthEntries);
        double minPrice = getMinPrice(lastMonthEntries);
        double maxPrice = getMaxPrice(lastMonthEntries);

        return new CryptoStatsDto(
                symbol,
                oldest.getPrice(),
                newest.getPrice(),
                minPrice,
                maxPrice
        );
    }

    public HighestNormalizedRangeDto getCryptoWithHighestNormalizedRange(String dateString) {
        LocalDate date = LocalDate.parse(dateString.trim());

        List<String> symbols = getAvailableSymbols();

        HighestNormalizedRangeDto result = null;
        double maxNormalizedRange = -1;

        for (String symbol : symbols) {
            List<CryptoPriceEntry> entries = getPriceDataFromCsvFile(symbol)
                    .stream()
                    .filter(e -> e.getDateTime().toLocalDate().equals(date))
                    .toList();

            if (entries.isEmpty()) {
                continue;
            }

            double min = getMinPrice(entries);
            double max = getMaxPrice(entries);
            double normalizedRange = (min > 0) ? (max - min) / min : 0;

            if (normalizedRange > maxNormalizedRange) {
                maxNormalizedRange = normalizedRange;
                result = new HighestNormalizedRangeDto(symbol, normalizedRange, dateString);
            }
        }

        return result;
    }

    private static double getMaxPrice(List<CryptoPriceEntry> entries) {
        return entries.stream()
                .mapToDouble(CryptoPriceEntry::getPrice)
                .max()
                .orElse(0);
    }

    private static double getMinPrice(List<CryptoPriceEntry> lastMonthEntries) {
        return lastMonthEntries.stream()
                .mapToDouble(CryptoPriceEntry::getPrice)
                .min()
                .orElse(0);
    }

    private static CryptoPriceEntry getNewest(List<CryptoPriceEntry> lastMonthEntries) {
        return lastMonthEntries.stream()
                .max(Comparator.comparing(CryptoPriceEntry::getDateTime))
                .get();
    }

    private static CryptoPriceEntry getOldest(List<CryptoPriceEntry> lastMonthEntries) {
        return lastMonthEntries.stream()
                .min(Comparator.comparing(CryptoPriceEntry::getDateTime))
                .get();
    }

    List<CryptoPriceEntry> getPriceDataFromCsvFile(String symbol) {
        List<CryptoPriceEntry> entries = new ArrayList<>();
        String fileName = symbol.toUpperCase() + "_values.csv";
        try (
                Reader reader = new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("prices/" + fileName));

        ) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);
            for (CSVRecord record : records) {
                long timestamp = Long.parseLong(record.get("timestamp"));
                double price = Double.parseDouble(record.get("price"));
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                entries.add(new CryptoPriceEntry(symbol, dateTime, price));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }

    List<CryptoPriceEntry> getPriceDataFromAllCsvFiles() {
        List<CryptoPriceEntry> entries = new ArrayList<>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Enumeration<URL> resources = classLoader.getResources("prices");
            while (resources.hasMoreElements()) {
                URL folderUrl = resources.nextElement();
                // For file system resources
                java.nio.file.Path folderPath = java.nio.file.Paths.get(folderUrl.toURI());
                java.nio.file.DirectoryStream<java.nio.file.Path> stream = java.nio.file.Files.newDirectoryStream(folderPath, "*.csv");
                for (java.nio.file.Path filePath : stream) {
                    try (Reader reader = java.nio.file.Files.newBufferedReader(filePath)) {
                        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .parse(reader);
                        for (CSVRecord record : records) {
                            long timestamp = Long.parseLong(record.get("timestamp"));
                            String symbol = record.get("symbol");
                            double price = Double.parseDouble(record.get("price"));
                            LocalDateTime dateTime = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                            entries.add(new CryptoPriceEntry(symbol, dateTime, price));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }

    List<String> getAvailableSymbols() {
        List<String> symbols = new ArrayList<>();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL folderUrl = classLoader.getResource("prices");
            if (folderUrl != null) {
                Path folderPath = Paths.get(folderUrl.toURI());
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, "*_values.csv")) {
                    for (Path filePath : stream) {
                        String fileName = filePath.getFileName().toString();
                        String symbol = fileName.substring(0, fileName.indexOf("_values.csv")).toUpperCase();
                        symbols.add(symbol);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return symbols;
    }
}
