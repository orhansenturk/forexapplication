package com.exchange.forex.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Response object containing currency conversion results")
public class ConversionResponse {

    @Schema(
        description = "Unique identifier for the conversion transaction",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String transactionId;

    @Schema(
        description = "The converted amount in target currency",
        example = "85.50"
    )
    private BigDecimal convertedAmount;

    @Schema(
        description = "Source currency code (ISO 4217)",
        example = "USD"
    )
    private String sourceCurrency;

    @Schema(
        description = "Target currency code (ISO 4217)",
        example = "EUR"
    )
    private String targetCurrency;

    @Schema(
        description = "Original amount in source currency",
        example = "100.00"
    )
    private BigDecimal sourceAmount;

    @Schema(
        description = "Exchange rate used for conversion",
        example = "0.855"
    )
    private double exchangeRate;

    @Schema(
        description = "Timestamp of the conversion",
        example = "2024-03-21T14:30:00"
    )
    private LocalDateTime conversionDate;
}
