package com.expensetracker.dto;

import com.expensetracker.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryDtos {
    public record CategoryRequest(
            @NotBlank String name,
            @NotNull TransactionType type,
            String color
    ) {}

    public record CategoryResponse(
            Long id,
            String name,
            TransactionType type,
            String color
    ) {}
}
