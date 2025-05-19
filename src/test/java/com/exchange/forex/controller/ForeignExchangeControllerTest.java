package com.exchange.forex.controller;

import com.exchange.forex.dto.request.ConversionRequest;
import com.exchange.forex.dto.response.ConversionResponse;
import com.exchange.forex.service.CurrencyConversionService;
import com.exchange.forex.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForeignExchangeControllerTest {

    @Mock
    private CurrencyConversionService conversionService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ForeignExchangeController controller;

    private ConversionRequest conversionRequest;
    private ConversionResponse conversionResponse;

    @BeforeEach
    void setUp() {
        conversionRequest = new ConversionRequest();
        conversionRequest.setAmount(BigDecimal.valueOf(100));
        conversionRequest.setSourceCurrency("USD");
        conversionRequest.setTargetCurrency("EUR");

        conversionResponse = new ConversionResponse();
        conversionResponse.setTransactionId("test-transaction");
        conversionResponse.setConvertedAmount(BigDecimal.valueOf(85));
        conversionResponse.setSourceCurrency("USD");
        conversionResponse.setTargetCurrency("EUR");
        conversionResponse.setSourceAmount(BigDecimal.valueOf(100));
        conversionResponse.setExchangeRate(0.85);
        conversionResponse.setConversionDate(LocalDateTime.now());
    }

    @Test
    void getExchangeRateSuccess() {
        when(exchangeRateService.getExchangeRate("USD", "EUR")).thenReturn(0.85);
        var response = controller.getExchangeRate("USD", "EUR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.85, response.getBody());
    }

    @Test
    void getExchangeRateInvalidCurrency() {
        when(exchangeRateService.getExchangeRate("INVALID", "EUR"))
            .thenThrow(new IllegalArgumentException("Invalid currency code"));

        var exception = assertThrows(ResponseStatusException.class,
            () -> controller.getExchangeRate("INVALID", "EUR"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid currency code", exception.getReason());
    }

    @Test
    void convertCurrencySuccess() {
        when(conversionService.convertCurrency(
            any(BigDecimal.class),
            anyString(),
            anyString()
        )).thenReturn(conversionResponse);

        var response = controller.convertCurrency(conversionRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversionResponse, response.getBody());
    }

    @Test
    void convertCurrencyError() {
        when(conversionService.convertCurrency(
            any(BigDecimal.class),
            anyString(),
            anyString()
        )).thenThrow(new IllegalArgumentException("Invalid currency"));

        var exception = assertThrows(ResponseStatusException.class,
            () -> controller.convertCurrency(conversionRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid currency", exception.getReason());
    }

    @Test
    void getConversionHistorySuccess() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("content", "test content");

        when(conversionService.getConversionHistory(
            anyString(),
            any(LocalDate.class),
            anyInt(),
            anyInt()
        )).thenReturn(mockResponse);

        var response = controller.getConversionHistory(
            "transaction-id",
            LocalDate.now(),
            0,
            10
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void getConversionHistoryBadRequestNullParameters() {
        var exception = assertThrows(ResponseStatusException.class,
            () -> controller.getConversionHistory(null, null, 0, 10));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Either transactionId or date must be provided", exception.getReason());

        verify(conversionService, never()).getConversionHistory(any(), any(), anyInt(), anyInt());
    }

    @Test
    void getConversionHistoryBadRequestServiceError() {
        doThrow(new IllegalArgumentException("Invalid parameters"))
            .when(conversionService).getConversionHistory(
                anyString(),
                any(LocalDate.class),
                anyInt(),
                anyInt()
            );

        var exception = assertThrows(ResponseStatusException.class,
            () -> controller.getConversionHistory("invalid-id", LocalDate.now(), 0, 10));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid parameters", exception.getReason());
    }

    @Test
    void bulkConvertCurrencySuccess() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("processedCount", 5);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        when(conversionService.processBulkConversions(any()))
            .thenReturn(mockResponse);

        var response = controller.bulkConvertCurrency(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void bulkConvertCurrencyEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.csv",
            MediaType.TEXT_PLAIN_VALUE,
            new byte[0]
        );

        var exception = assertThrows(ResponseStatusException.class,
            () -> controller.bulkConvertCurrency(emptyFile));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("File is empty", exception.getReason());
    }
}