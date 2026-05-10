package com.expensetracker.repository;

import com.expensetracker.model.Budget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category"})
    List<Budget> findByUserIdAndMonthOrderByCategory_Name(Long userId, String month);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    Optional<Budget> findByUserIdAndCategoryIdAndMonth(Long userId, Long categoryId, String month);
}
