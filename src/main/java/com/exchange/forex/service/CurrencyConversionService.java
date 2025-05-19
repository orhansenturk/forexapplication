package com.exchange.forex.service;

import com.exchange.forex.dto.request.ConversionRequest;
import com.exchange.forex.dto.response.ConversionResponse;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface CurrencyConversionService {
    ConversionResponse convertCurrency(BigDecimal amount, String sourceCurrency, String targetCurrency);
    Map<String, Object> getConversionHistory(String transactionId, LocalDate date, int page, int size);
    Map<String, Object> processBulkConversions(InputStream inputStream);
}
