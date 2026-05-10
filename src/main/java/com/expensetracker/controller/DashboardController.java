package com.expensetracker.controller;

import com.expensetracker.dto.DashboardDtos.DashboardResponse;
import com.expensetracker.security.CurrentUser;
import com.expensetracker.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse get(
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(required = false) String month
    ) {
        return dashboardService.get(user.id(), month);
    }
}
