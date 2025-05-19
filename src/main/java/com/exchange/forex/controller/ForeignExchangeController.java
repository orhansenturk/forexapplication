package com.exchange.forex.controller;

import com.exchange.forex.dto.request.ConversionRequest;
import com.exchange.forex.dto.response.ConversionResponse;
import com.exchange.forex.service.CurrencyConversionService;
import com.exchange.forex.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Forex Exchange API", description = "API endpoints for currency conversion and exchange rate operations")
public class ForeignExchangeController {
    private final CurrencyConversionService conversionService;
    private final ExchangeRateService exchangeRateService;

    @Operation(
        summary = "Get current exchange rate",
        description = "Retrieves the current exchange rate between two currencies using ISO 4217 currency codes"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Exchange rate retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid currency codes provided"),
        @ApiResponse(responseCode = "500", description = "Error occurred while fetching exchange rate")
    })
    @GetMapping("/exchange-rate")
    public ResponseEntity<Double> getExchangeRate(
        @Parameter(description = "Source currency code (ISO 4217)", example = "USD")
        @RequestParam String sourceCurrency,
        @Parameter(description = "Target currency code (ISO 4217)", example = "EUR")
        @RequestParam String targetCurrency
    ) {
        try {
            Double exchangeRate = exchangeRateService.getExchangeRate(sourceCurrency, targetCurrency);
            return ResponseEntity.ok(exchangeRate);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting exchange rate: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Convert currency",
        description = "Converts an amount from one currency to another and stores the conversion history"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Currency converted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConversionResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Error occurred during conversion")
    })
    @PostMapping(value = "/convert", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConversionResponse> convertCurrency(
        @Parameter(description = "Currency conversion request details", required = true)
        @Valid @RequestBody ConversionRequest request
    ) {
        try {
            ConversionResponse response = conversionService.convertCurrency(
                request.getAmount(),
                request.getSourceCurrency(),
                request.getTargetCurrency()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while converting currency: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Get conversion history",
        description = "Retrieves paginated conversion history filtered by transaction ID or date"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conversion history retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "500", description = "Error occurred while retrieving history")
    })
    @GetMapping(value = "/conversions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getConversionHistory(
        @Parameter(description = "Transaction ID to filter by")
        @RequestParam(required = false) String transactionId,
        @Parameter(description = "Date to filter by (ISO format: YYYY-MM-DD)", example = "2024-03-21")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of records per page", example = "10")
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            if (transactionId == null && date == null) {
                throw new IllegalArgumentException("Either transactionId or date must be provided");
            }
            Map<String, Object> response = conversionService.getConversionHistory(transactionId, date, page, size);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while retrieving history: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Bulk currency conversion",
        description = "Converts multiple currency amounts using a CSV file. The CSV should have columns: " +
            "amount,sourceCurrency,targetCurrency"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bulk conversion completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid CSV file or format"),
        @ApiResponse(responseCode = "500", description = "Error occurred during bulk conversion")
    })
    @PostMapping(value = "/bulk-convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> bulkConvertCurrency(
        @Parameter(
            description = "CSV file containing conversion requests. Format: amount,sourceCurrency,targetCurrency",
            required = true
        )
        @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        try {
            Map<String, Object> response = conversionService.processBulkConversions(file.getInputStream());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during bulk conversion: " + e.getMessage());
        }
    }
}
