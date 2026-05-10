package com.expensetracker.controller;

import com.expensetracker.dto.AuthDtos.AuthResponse;
import com.expensetracker.dto.AuthDtos.LoginRequest;
import com.expensetracker.dto.AuthDtos.RegisterRequest;
import com.expensetracker.dto.AuthDtos.UserResponse;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.CurrentUser;
import com.expensetracker.service.AuthService;
import com.expensetracker.service.Mapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/refresh")
    public AuthResponse refresh(@Valid @RequestBody com.expensetracker.dto.AuthDtos.RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/auth/forgot-password")
    public void forgotPassword(@Valid @RequestBody com.expensetracker.dto.AuthDtos.ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
    }

    @PostMapping("/auth/reset-password")
    public void resetPassword(@Valid @RequestBody com.expensetracker.dto.AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return userRepository.findById(currentUser.id()).map(Mapper::user).orElseThrow();
    }
}
