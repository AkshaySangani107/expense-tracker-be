package com.expensetracker.controller;

import com.expensetracker.dto.TransactionDtos.TransactionRequest;
import com.expensetracker.dto.TransactionDtos.TransactionResponse;
import com.expensetracker.security.CurrentUser;
import com.expensetracker.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> list(
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(required = false) String month
    ) {
        return transactionService.list(user.id(), month);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(
            @AuthenticationPrincipal CurrentUser user,
            @Valid @RequestBody TransactionRequest request
    ) {
        return transactionService.create(user.id(), request);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(
            @AuthenticationPrincipal CurrentUser user,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request
    ) {
        return transactionService.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        transactionService.delete(user.id(), id);
    }
}
