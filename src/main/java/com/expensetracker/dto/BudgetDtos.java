package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BudgetDtos {
    public record BudgetRequest(
            @NotNull Long categoryId,
            @NotBlank String month,
            @NotNull @DecimalMin(value = "0.01") BigDecimal limitAmount
    ) {}

    public record BudgetResponse(
            Long id,
            Long categoryId,
            String categoryName,
            String categoryColor,
            String month,
            BigDecimal limitAmount,
            BigDecimal spentAmount,
            BigDecimal remainingAmount
    ) {}
}
