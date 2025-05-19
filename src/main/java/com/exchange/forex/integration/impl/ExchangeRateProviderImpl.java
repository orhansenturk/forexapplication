package com.exchange.forex.integration.impl;

import com.exchange.forex.exception.ExternalServiceException;
import com.exchange.forex.integration.ExchangeRateProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateProviderImpl implements ExchangeRateProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${exchange-rate.api.url}")
    private String apiUrl;
    @Value("${exchange-rate.api.key}")
    private String apiKey;


    @Override
    public double getExchangeRate(String sourceCurrency, String targetCurrency) throws ExternalServiceException {
        String url = String.format("%s/latest/%s?apikey=%s", apiUrl, sourceCurrency, apiKey);

        Map<String, Object> response;
        try {
            response = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            throw new ExternalServiceException("Error fetching exchange rate from Exchange Rate API: " + e.getMessage());
        }

        try {
            if (response == null || !response.containsKey("rates")) {
                throw new ExternalServiceException("Failed to fetch exchange rate from external API");
            }

            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            Double rate = rates.get(targetCurrency);

            if (rate == null) {
                throw new ExternalServiceException("Exchange rate not available for the specified currency pair");
            }
            return rate;
        } catch (Exception e) {
            throw new ExternalServiceException("Error parsing API response: Invalid data format");
        }
    }
}
