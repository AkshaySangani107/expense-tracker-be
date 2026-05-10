package com.expensetracker.controller;

import com.expensetracker.dto.BudgetDtos.BudgetRequest;
import com.expensetracker.dto.BudgetDtos.BudgetResponse;
import com.expensetracker.security.CurrentUser;
import com.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> list(
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(required = false) String month) {
        return budgetService.list(user.id(), month);
    }

    @PostMapping
    public BudgetResponse upsert(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody BudgetRequest request) {
        return budgetService.upsert(user.id(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        budgetService.delete(user.id(), id);
    }
}
