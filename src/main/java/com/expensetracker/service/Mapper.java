package com.expensetracker.service;

import com.expensetracker.dto.AuthDtos.UserResponse;
import com.expensetracker.dto.BudgetDtos.BudgetResponse;
import com.expensetracker.dto.CategoryDtos.CategoryResponse;
import com.expensetracker.dto.TransactionDtos.TransactionResponse;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.User;
import java.math.BigDecimal;

public final class Mapper {
    private Mapper() {}

    public static UserResponse user(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public static CategoryResponse category(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.getColor());
    }

    public static TransactionResponse transaction(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getCategory().getColor(),
                transaction.getTransactionDate(),
                transaction.getNote(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public static BudgetResponse budget(Budget budget, BigDecimal spent) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getCategory().getColor(),
                budget.getMonth(),
                budget.getLimitAmount(),
                spent,
                budget.getLimitAmount().subtract(spent)
        );
    }
}
