package com.exchange.forex.integration;

import com.exchange.forex.exception.ExternalServiceException;

public interface ExchangeRateProvider {
    double getExchangeRate(String sourceCurrency, String targetCurrency) throws ExternalServiceException;
}