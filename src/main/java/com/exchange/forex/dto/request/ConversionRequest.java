package com.exchange.forex.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object for currency conversion operations")
public class ConversionRequest {

    @Schema(
        description = "Amount to convert",
        example = "100.50",
        minimum = "0.01",
        required = true
    )
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(
        description = "Source currency code (ISO 4217)",
        example = "USD",
        minLength = 3,
        maxLength = 3,
        required = true
    )
    @NotBlank(message = "Source currency is required")
    @Size(min = 3, max = 3, message = "Source currency must be 3 characters")
    private String sourceCurrency;

    @Schema(
        description = "Target currency code (ISO 4217)",
        example = "EUR",
        minLength = 3,
        maxLength = 3,
        required = true
    )
    @NotBlank(message = "Target currency is required")
    @Size(min = 3, max = 3, message = "Target currency must be 3 characters")
    private String targetCurrency;
}
