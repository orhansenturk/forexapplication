package com.exchange.forex.service.impl;

import com.exchange.forex.dto.response.ConversionResponse;
import com.exchange.forex.exception.ExternalServiceException;
import com.exchange.forex.integration.impl.ExchangeRateProviderService;
import com.exchange.forex.service.CurrencyConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceImplTest {

    @Mock
    private ExchangeRateProviderService exchangeRateProviderService;

    @InjectMocks
    private CurrencyConversionService conversionService;

    private static final String SOURCE_CURRENCY = "USD";
    private static final String TARGET_CURRENCY = "EUR";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private static final double EXCHANGE_RATE = 0.85;

    @Test
    void convertCurrencySuccess() throws ExternalServiceException {
        when(exchangeRateProviderService.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY))
            .thenReturn(EXCHANGE_RATE);

        ConversionResponse response = conversionService.convertCurrency(AMOUNT, SOURCE_CURRENCY, TARGET_CURRENCY);

        assertNotNull(response);
        assertEquals(SOURCE_CURRENCY, response.getSourceCurrency());
        assertEquals(TARGET_CURRENCY, response.getTargetCurrency());
        assertEquals(AMOUNT, response.getSourceAmount());
        assertEquals(BigDecimal.valueOf(85.00).setScale(2), response.getConvertedAmount());
        assertEquals(EXCHANGE_RATE, response.getExchangeRate());
        assertNotNull(response.getTransactionId());
        assertNotNull(response.getConversionDate());
    }

    @Test
    void convertCurrencyInvalidAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> conversionService.convertCurrency(BigDecimal.valueOf(-100), SOURCE_CURRENCY, TARGET_CURRENCY));

        assertThrows(IllegalArgumentException.class,
            () -> conversionService.convertCurrency(null, SOURCE_CURRENCY, TARGET_CURRENCY));
    }

    @Test
    void convertCurrencyInvalidCurrency() {
        assertThrows(IllegalArgumentException.class,
            () -> conversionService.convertCurrency(AMOUNT, "INVALID", TARGET_CURRENCY));

        assertThrows(IllegalArgumentException.class,
            () -> conversionService.convertCurrency(AMOUNT, SOURCE_CURRENCY, "INVALID"));
    }

    @Test
    void getConversionHistoryByTransactionId() throws ExternalServiceException {
        // First perform a conversion to have something in history
        when(exchangeRateProviderService.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY))
            .thenReturn(EXCHANGE_RATE);
        ConversionResponse conversion = conversionService.convertCurrency(AMOUNT, SOURCE_CURRENCY, TARGET_CURRENCY);

        Map<String, Object> history = conversionService.getConversionHistory(
            conversion.getTransactionId(),
            null,
            0,
            10
        );

        assertNotNull(history);
        assertTrue(history.containsKey("content"));
        assertTrue(history.containsKey("pageable"));
    }

    @Test
    void getConversionHistoryByDate() throws ExternalServiceException {
        // First perform a conversion to have something in history
        when(exchangeRateProviderService.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY))
            .thenReturn(EXCHANGE_RATE);
        conversionService.convertCurrency(AMOUNT, SOURCE_CURRENCY, TARGET_CURRENCY);

        Map<String, Object> history = conversionService.getConversionHistory(
            null,
            LocalDate.now(),
            0,
            10
        );

        assertNotNull(history);
        assertTrue(history.containsKey("content"));
        assertTrue(history.containsKey("pageable"));
    }

    @Test
    void getConversionHistoryNoParams() {
        assertThrows(IllegalArgumentException.class,
            () -> conversionService.getConversionHistory(null, null, 0, 10));
    }

    @Test
    void processBulkConversionsSuccess() throws IOException, ExternalServiceException {
        when(exchangeRateProviderService.getExchangeRate(SOURCE_CURRENCY, TARGET_CURRENCY))
            .thenReturn(EXCHANGE_RATE);

        String csvContent = "amount,sourceCurrency,targetCurrency\n" +
            "100,USD,EUR\n" +
            "200,USD,EUR";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        Map<String, Object> result = conversionService.processBulkConversions(file.getInputStream());

        assertNotNull(result);
        assertEquals(2, result.get("processedCount"));
        assertNotNull(result.get("successfulConversions"));
        assertNotNull(result.get("failedConversions"));
    }

    @Test
    void processBulkConversionsInvalidData() throws IOException {
        String csvContent = "amount,sourceCurrency,targetCurrency\n" +
            "-100,USD,EUR\n" +  // Invalid amount
            "abc,USD,EUR";      // Invalid number format

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        Map<String, Object> result = conversionService.processBulkConversions(file.getInputStream());

        assertNotNull(result);
        assertEquals(2, result.get("processedCount"));
        assertTrue(((List<?>)result.get("failedConversions")).size() > 0);
    }
}