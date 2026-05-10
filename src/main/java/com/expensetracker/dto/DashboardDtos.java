package com.expensetracker.dto;

import com.expensetracker.dto.BudgetDtos.BudgetResponse;
import com.expensetracker.dto.TransactionDtos.TransactionResponse;
import java.math.BigDecimal;
import java.util.List;

public class DashboardDtos {
    public record CategoryTotal(Long categoryId, String categoryName, String color, BigDecimal amount) {}

    public record DashboardResponse(
            String month,
            BigDecimal income,
            BigDecimal expense,
            BigDecimal balance,
            List<CategoryTotal> expenseByCategory,
            List<BudgetResponse> budgets,
            List<TransactionResponse> recentTransactions
    ) {}
}
