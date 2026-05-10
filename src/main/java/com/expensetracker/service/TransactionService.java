package com.expensetracker.service;

import com.expensetracker.dto.TransactionDtos.TransactionRequest;
import com.expensetracker.dto.TransactionDtos.TransactionResponse;
import com.expensetracker.exception.ApiException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.User;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryService categoryService,
            UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(Long userId, String month) {
        YearMonth yearMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end)
                .stream()
                .map(Mapper::transaction)
                .toList();
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Category category = categoryService.getOwned(userId, request.categoryId());
        validateCategoryType(category, request);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        apply(transaction, category, request);
        return Mapper.transaction(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = getOwned(userId, id);
        Category category = categoryService.getOwned(userId, request.categoryId());
        validateCategoryType(category, request);
        apply(transaction, category, request);
        return Mapper.transaction(transaction);
    }

    public void delete(Long userId, Long id) {
        transactionRepository.delete(getOwned(userId, id));
    }

    Transaction getOwned(Long userId, Long id) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    private void apply(Transaction transaction, Category category, TransactionRequest request) {
        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setCategory(category);
        transaction.setTransactionDate(request.transactionDate());
        transaction.setNote(request.note() == null ? null : request.note().trim());
    }

    private void validateCategoryType(Category category, TransactionRequest request) {
        if (category.getType() != request.type()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category type must match transaction type");
        }
    }
}
