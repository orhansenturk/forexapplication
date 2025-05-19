package com.exchange.forex.service.impl;

import com.exchange.forex.dto.response.ConversionResponse;
import com.exchange.forex.exception.ExternalServiceException;
import com.exchange.forex.integration.ExchangeRateProvider;
import com.exchange.forex.service.CurrencyConversionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private final Map<String, ConversionResponse> conversionHistory = new ConcurrentHashMap<>();

    private final ExchangeRateProvider exchangeRateProvider;

    @Override
    public ConversionResponse convertCurrency(BigDecimal amount, String sourceCurrency, String targetCurrency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (sourceCurrency == null || sourceCurrency.length() != 3) {
            throw new IllegalArgumentException("Source currency must be a 3-character code.");
        }
        if (targetCurrency == null || targetCurrency.length() != 3) {
            throw new IllegalArgumentException("Target currency must be a 3-character code.");
        }

        double exchangeRate;
        try {
            exchangeRate = exchangeRateProvider.getExchangeRate(sourceCurrency, targetCurrency);
        } catch (ExternalServiceException e) {
            throw new RuntimeException("Failed to convert currency: " + e.getMessage(), e);
        }

        BigDecimal convertedAmount = amount.multiply(BigDecimal.valueOf(exchangeRate)).setScale(2, RoundingMode.HALF_UP);
        String transactionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        ConversionResponse response = new ConversionResponse();
        response.setTransactionId(transactionId);
        response.setConvertedAmount(convertedAmount);
        response.setSourceCurrency(sourceCurrency);
        response.setTargetCurrency(targetCurrency);
        response.setSourceAmount(amount);
        response.setExchangeRate(exchangeRate);
        response.setConversionDate(now);

        conversionHistory.put(transactionId, response);
        return response;
    }

    @Override
    public Map<String, Object> getConversionHistory(String transactionId, LocalDate date, int page, int size) {
        if (transactionId == null && date == null) {
            throw new IllegalArgumentException("Either transactionId or date must be provided");
        }

        try {
            List<ConversionResponse> filteredList = new ArrayList<>(conversionHistory.values());

            if (transactionId != null) {
                filteredList = filteredList.stream()
                    .filter(conversion -> conversion.getTransactionId().equals(transactionId))
                    .collect(Collectors.toList());
            } else if (date != null) {
                final LocalDate filterDate = date;
                filteredList = filteredList.stream()
                    .filter(conversion -> conversion.getConversionDate().toLocalDate().equals(filterDate))
                    .collect(Collectors.toList());
            }

            int totalItems = filteredList.size();
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), totalItems);

            List<ConversionResponse> pagedList = (start > totalItems) ? Collections.emptyList() : filteredList.subList(start, end);

            Map<String, Object> result = new HashMap<>();
            result.put("content", pagedList);
            result.put("pageable", Map.of(
                "pageNumber", pageable.getPageNumber(),
                "pageSize", pageable.getPageSize(),
                "totalElements", totalItems,
                "totalPages", (int) Math.ceil((double) totalItems / pageable.getPageSize())
            ));
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing conversion history: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> processBulkConversions(InputStream inputStream) {
        List<ConversionResponse> successfulConversions = new ArrayList<>();
        List<Map<String, Object>> failedConversions = new ArrayList<>();
        int processedCount = 0;

        try (Reader reader = new InputStreamReader(inputStream);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            for (CSVRecord record : parser) {
                processedCount++;
                try {
                    BigDecimal amount = new BigDecimal(record.get("amount"));
                    String sourceCurrency = record.get("sourceCurrency");
                    String targetCurrency = record.get("targetCurrency");
                    ConversionResponse response = convertCurrency(amount, sourceCurrency, targetCurrency);
                    successfulConversions.add(response);
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("rowNumber", processedCount);
                    error.put("errorMessage", e.getMessage());
                    failedConversions.add(error);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing CSV file: " + e.getMessage(), e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processedCount", processedCount);
        result.put("successfulConversions", successfulConversions);
        result.put("failedConversions", failedConversions);
        return result;
    }
}
