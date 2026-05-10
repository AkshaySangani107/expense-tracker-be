package com.expensetracker.dto;

import com.expensetracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class TransactionDtos {
    public record TransactionRequest(
            @NotNull TransactionType type,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotNull Long categoryId,
            @NotNull LocalDate transactionDate,
            @Size(max = 500) String note
    ) {}

    public record TransactionResponse(
            Long id,
            TransactionType type,
            BigDecimal amount,
            Long categoryId,
            String categoryName,
            String categoryColor,
            LocalDate transactionDate,
            String note,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
