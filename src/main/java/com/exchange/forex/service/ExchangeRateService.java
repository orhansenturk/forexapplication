package com.exchange.forex.service;

public interface ExchangeRateService {
    double getExchangeRate(String sourceCurrency, String targetCurrency);
}
