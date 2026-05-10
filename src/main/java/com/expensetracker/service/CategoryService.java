package com.expensetracker.service;

import com.expensetracker.dto.CategoryDtos.CategoryRequest;
import com.expensetracker.dto.CategoryDtos.CategoryResponse;
import com.expensetracker.exception.ApiException;
import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            BudgetRepository budgetRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public List<CategoryResponse> list(Long userId) {
        return categoryRepository.findByUserIdOrderByName(userId).stream().map(Mapper::category).toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, request.name().trim(), request.type())) {
            throw new ApiException(HttpStatus.CONFLICT, "Category already exists");
        }
        User user = userRepository.findById(userId).orElseThrow();
        Category category = new Category();
        category.setUser(user);
        category.setName(request.name().trim());
        category.setType(request.type());
        category.setColor(normalizeColor(request.color()));
        return Mapper.category(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long id, CategoryRequest request) {
        Category category = getOwned(userId, id);
        category.setName(request.name().trim());
        category.setType(request.type());
        category.setColor(normalizeColor(request.color()));
        return Mapper.category(category);
    }

    public void delete(Long userId, Long id) {
        if (transactionRepository.existsByUserIdAndCategoryId(userId, id)
                || budgetRepository.existsByUserIdAndCategoryId(userId, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Category is in use");
        }
        categoryRepository.delete(getOwned(userId, id));
    }

    Category getOwned(Long userId, Long id) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private String normalizeColor(String color) {
        return color == null || color.isBlank() ? "#2563eb" : color.trim();
    }
}
