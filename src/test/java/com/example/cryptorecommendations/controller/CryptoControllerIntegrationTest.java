package com.example.cryptorecommendations.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CryptoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCryptosSortedByNormalizedRange_success() throws Exception {
        mockMvc.perform(get("/cryptos/normalized-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].symbol", not(emptyString())))
                .andExpect(jsonPath("$[0].normalizedRange", notNullValue()));
    }

    @Test
    void getCryptoStats_success() throws Exception {
        mockMvc.perform(get("/cryptos/BTC/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.oldestPrice", notNullValue()))
                .andExpect(jsonPath("$.newestPrice", notNullValue()))
                .andExpect(jsonPath("$.minPrice", notNullValue()))
                .andExpect(jsonPath("$.maxPrice", notNullValue()));
    }

    @Test
    void getCryptoStats_notFound() throws Exception {
        mockMvc.perform(get("/cryptos/FAKECOIN/stats"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("doesn't exist")));
    }

    @Test
    void getCryptoStatsForPeriod_success() throws Exception {
        mockMvc.perform(get("/cryptos/BTC/stats/period")
                        .param("period", "10")
                        .param("periodType", "YEARS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"));
    }

    @Test
    void getCryptoStatsForPeriod_noDataInPeriod() throws Exception {
        mockMvc.perform(get("/cryptos/BTC/stats/period")
                        .param("period", "1")
                        .param("periodType", "YEARS"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No data")));
    }

    @Test
    void getCryptoWithHighestNormalizedRange_success() throws Exception {
        mockMvc.perform(get("/cryptos/highest-normalized-range")
                        .param("date", "2022-01-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", not(emptyString())))
                .andExpect(jsonPath("$.normalizedRange", notNullValue()))
                .andExpect(jsonPath("$.date", is("2022-01-13")));
    }

    @Test
    void getCryptoWithHighestNormalizedRange_noData() throws Exception {
        mockMvc.perform(get("/cryptos/highest-normalized-range")
                        .param("date", "1900-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").doesNotExist());
    }
}