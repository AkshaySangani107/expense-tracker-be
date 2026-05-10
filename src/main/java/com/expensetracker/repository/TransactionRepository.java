package com.expensetracker.repository;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category"})
    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId,
            LocalDate start,
            LocalDate end
    );

    @Query("""
            select sum(t.amount)
            from Transaction t
            where t.user.id = :userId
              and t.type = :type
              and t.transactionDate between :start and :end
            """)
    BigDecimal sumByUserTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            select t.category.id, t.category.name, t.category.color, sum(t.amount)
            from Transaction t
            where t.user.id = :userId
              and t.type = :type
              and t.transactionDate between :start and :end
            group by t.category.id, t.category.name, t.category.color
            order by sum(t.amount) desc
            """)
    List<Object[]> sumByCategory(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            select sum(t.amount)
            from Transaction t
            where t.user.id = :userId
              and t.category.id = :categoryId
              and t.type = com.expensetracker.model.TransactionType.EXPENSE
              and t.transactionDate between :start and :end
            """)
    BigDecimal sumExpenseForCategory(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
