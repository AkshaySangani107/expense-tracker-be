package com.expensetracker.service;

import com.expensetracker.dto.DashboardDtos.CategoryTotal;
import com.expensetracker.dto.DashboardDtos.DashboardResponse;
import com.expensetracker.model.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public DashboardService(TransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse get(Long userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month == null || month.isBlank() ? YearMonth.now().toString() : month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        BigDecimal income = transactionRepository.sumByUserTypeAndDateRange(userId, TransactionType.INCOME, start, end);
        if (income == null) income = BigDecimal.ZERO;
        
        BigDecimal expense = transactionRepository.sumByUserTypeAndDateRange(userId, TransactionType.EXPENSE, start, end);
        if (expense == null) expense = BigDecimal.ZERO;
        
        var transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end)
                .stream()
                .limit(5)
                .map(Mapper::transaction)
                .toList();
        List<CategoryTotal> categoryTotals = transactionRepository
                .sumByCategory(userId, TransactionType.EXPENSE, start, end)
                .stream()
                .map(row -> new CategoryTotal((Long) row[0], (String) row[1], (String) row[2], new BigDecimal(row[3].toString())))
                .toList();
        return new DashboardResponse(
                yearMonth.toString(),
                income,
                expense,
                income.subtract(expense),
                categoryTotals,
                budgetService.list(userId, yearMonth.toString()),
                transactions
        );
    }
}
