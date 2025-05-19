package com.exchange.forex.service;

import com.exchange.forex.exception.ExternalServiceException;
import com.exchange.forex.integration.impl.ExchangeRateProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateProviderService exchangeRateProviderService;

    public double getExchangeRate(String sourceCurrency, String targetCurrency) {
        if (sourceCurrency == null || sourceCurrency.length() != 3) {
            throw new IllegalArgumentException("Source currency must be a 3-character code.");
        }
        if (targetCurrency == null || targetCurrency.length() != 3) {
            throw new IllegalArgumentException("Target currency must be a 3-character code.");
        }
        try {
            return exchangeRateProviderService.getExchangeRate(sourceCurrency, targetCurrency);
        } catch (ExternalServiceException e) {
            throw new RuntimeException("Failed to get exchange rate: " + e.getMessage(), e);
        }
    }
}
