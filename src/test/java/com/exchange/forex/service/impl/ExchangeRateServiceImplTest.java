package com.exchange.forex.service.impl;

import com.exchange.forex.exception.ExternalServiceException;
import com.exchange.forex.integration.ExchangeRateProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;

    private static final String SOURCE_CURRENCY = "USD";
    private static final String TARGET_CURRENCY = "EUR";
    private static final double EXCHANGE_RATE = 0.85;

    @Test
    void getExchangeRateSuccess() throws ExternalServiceException {
        when(exchangeRateProvider.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY))
            .thenReturn(EXCHANGE_RATE);

        double rate = exchangeRateService.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY);
        assertEquals(EXCHANGE_RATE, rate);
    }

    @Test
    void getExchangeRateNullSourceCurrency() {
        assertThrows(IllegalArgumentException.class,
            () -> exchangeRateService.getExchangeRate(null, TARGET_CURRENCY));
    }

    @Test
    void getExchangeRateNullTargetCurrency() {
        assertThrows(IllegalArgumentException.class,
            () -> exchangeRateService.getExchangeRate(SOURCE_CURRENCY, null));
    }

    @Test
    void getExchangeRateInvalidSourceCurrencyLength() {
        assertThrows(IllegalArgumentException.class,
            () -> exchangeRateService.getExchangeRate("USDD", TARGET_CURRENCY));
    }

    @Test
    void getExchangeRateInvalidTargetCurrencyLength() {
        assertThrows(IllegalArgumentException.class,
            () -> exchangeRateService.getExchangeRate(SOURCE_CURRENCY, "EU"));
    }

    @Test
    void getExchangeRateExternalServiceError() throws ExternalServiceException {
        when(exchangeRateProvider.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY))
            .thenThrow(new ExternalServiceException("API Error"));

        assertThrows(RuntimeException.class,
            () -> exchangeRateService.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY));
    }
}