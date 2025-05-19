package com.exchange.forex.integration.impl;

import com.exchange.forex.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateProviderImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExchangeRateProviderImpl exchangeRateProvider;

    private static final String SOURCE_CURRENCY = "USD";
    private static final String TARGET_CURRENCY = "EUR";
    private static final String API_URL = "https://api.exchangerate-api.com/v4";
    private static final String API_KEY = "test-api-key";
    private static final double EXCHANGE_RATE = 0.85;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeRateProvider, "apiUrl", API_URL);
        ReflectionTestUtils.setField(exchangeRateProvider, "apiKey", API_KEY);
    }

    @Test
    void getExchangeRateSuccess() throws ExternalServiceException {
        Map<String, Object> response = new HashMap<>();
        Map<String, Double> rates = new HashMap<>();
        rates.put(TARGET_CURRENCY, EXCHANGE_RATE);
        response.put("rates", rates);

        String expectedUrl = String.format("%s/latest/%s?apikey=%s", API_URL, SOURCE_CURRENCY, API_KEY);
        when(restTemplate.getForObject(expectedUrl, Map.class)).thenReturn(response);

        double rate = exchangeRateProvider.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY);
        assertEquals(EXCHANGE_RATE, rate);
    }

    @Test
    void getExchangeRateApiError() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenThrow(new RestClientException("API Error"));

        assertThrows(ExternalServiceException.class,
            () -> exchangeRateProvider.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY));
    }

    @Test
    void getExchangeRateNullResponse() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThrows(ExternalServiceException.class,
            () -> exchangeRateProvider.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY));
    }

    @Test
    void getExchangeRateMissingRates() {
        Map<String, Object> response = new HashMap<>();
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThrows(ExternalServiceException.class,
            () -> exchangeRateProvider.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY));
    }

    @Test
    void getExchangeRateInvalidRatesFormat() {
        Map<String, Object> response = new HashMap<>();
        response.put("rates", "INVALID");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        assertThrows(ExternalServiceException.class,
            () -> exchangeRateProvider.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY));
    }
}