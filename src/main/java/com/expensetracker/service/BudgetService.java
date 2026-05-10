package com.expensetracker.service;

import com.expensetracker.dto.BudgetDtos.BudgetRequest;
import com.expensetracker.dto.BudgetDtos.BudgetResponse;
import com.expensetracker.exception.ApiException;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            TransactionRepository transactionRepository,
            CategoryService categoryService,
            UserRepository userRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(Long userId, String month) {
        String targetMonth = normalizeMonth(month);
        return budgetRepository.findByUserIdAndMonthOrderByCategory_Name(userId, targetMonth).stream()
                .map(budget -> Mapper.budget(budget, spent(userId, budget.getCategory().getId(), targetMonth)))
                .toList();
    }

    @Transactional
    public BudgetResponse upsert(Long userId, BudgetRequest request) {
        String month = normalizeMonth(request.month());
        Category category = categoryService.getOwned(userId, request.categoryId());
        if (category.getType() != TransactionType.EXPENSE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Budgets can only be set for expense categories");
        }
        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndMonth(userId, category.getId(), month)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    Budget created = new Budget();
                    created.setUser(user);
                    created.setCategory(category);
                    created.setMonth(month);
                    return created;
                });
        budget.setLimitAmount(request.limitAmount());
        Budget saved = budgetRepository.save(budget);
        return Mapper.budget(saved, spent(userId, category.getId(), month));
    }

    public void delete(Long userId, Long id) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Budget not found"));
        budgetRepository.delete(budget);
    }

    private BigDecimal spent(Long userId, Long categoryId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        BigDecimal spent = transactionRepository.sumExpenseForCategory(userId, categoryId, start, end);
        return spent != null ? spent : BigDecimal.ZERO;
    }

    private String normalizeMonth(String month) {
        return YearMonth.parse(month == null || month.isBlank() ? YearMonth.now().toString() : month).toString();
    }
}
