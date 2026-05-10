package com.expensetracker.service;

import com.expensetracker.dto.AuthDtos.AuthResponse;
import com.expensetracker.dto.AuthDtos.LoginRequest;
import com.expensetracker.dto.AuthDtos.RegisterRequest;
import com.expensetracker.exception.ApiException;
import com.expensetracker.model.Category;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.model.RefreshToken;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.expensetracker.repository.PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            com.expensetracker.repository.PasswordResetTokenRepository passwordResetTokenRepository,
            MailService mailService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailService = mailService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        seedCategories(saved);
        return authResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return authResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(com.expensetracker.dto.AuthDtos.RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.refreshToken());
        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();
        
        refreshTokenService.deleteByUserId(user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        
        return new AuthResponse(
                jwtService.generateToken(user.getId(), user.getEmail()),
                newRefreshToken.getToken(),
                Mapper.user(user)
        );
    }

    private AuthResponse authResponse(User user) {
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new AuthResponse(
                jwtService.generateToken(user.getId(), user.getEmail()),
                refreshToken.getToken(),
                Mapper.user(user)
        );
    }

    private void seedCategories(User user) {
        List<SeedCategory> seeds = List.of(
                new SeedCategory("Food", TransactionType.EXPENSE, "#FF4757"),
                new SeedCategory("Travel", TransactionType.EXPENSE, "#FFA502"),
                new SeedCategory("Rent", TransactionType.EXPENSE, "#A55EEA"),
                new SeedCategory("Shopping", TransactionType.EXPENSE, "#FF6B81"),
                new SeedCategory("Bills", TransactionType.EXPENSE, "#1E90FF"),
                new SeedCategory("Health", TransactionType.EXPENSE, "#2ED573"),
                new SeedCategory("Entertainment", TransactionType.EXPENSE, "#ECCC68"),
                new SeedCategory("Other", TransactionType.EXPENSE, "#A4B0BE"),
                new SeedCategory("Salary", TransactionType.INCOME, "#2ED573"),
                new SeedCategory("Freelance", TransactionType.INCOME, "#1DD1A1"),
                new SeedCategory("Other Income", TransactionType.INCOME, "#70A1FF")
        );
        seeds.forEach(seed -> {
            Category category = new Category();
            category.setUser(user);
            category.setName(seed.name());
            category.setType(seed.type());
            category.setColor(seed.color());
            categoryRepository.save(category);
        });
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        
        passwordResetTokenRepository.deleteByUserId(user.getId());
        
        String tokenStr = java.util.UUID.randomUUID().toString();
        com.expensetracker.model.PasswordResetToken resetToken = new com.expensetracker.model.PasswordResetToken();
        resetToken.setToken(tokenStr);
        resetToken.setUser(user);
        resetToken.setExpiryDate(java.time.Instant.now().plus(1, java.time.temporal.ChronoUnit.HOURS));
        passwordResetTokenRepository.save(resetToken);
        System.out.println("forgotPassword: " + resetToken);
        mailService.sendPasswordResetMail(user.getEmail(), tokenStr);
    }

    @Transactional
    public void resetPassword(String tokenStr, String newPassword) {
        com.expensetracker.model.PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid token"));
                
        if (resetToken.getExpiryDate().isBefore(java.time.Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token expired");
        }
        
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        passwordResetTokenRepository.delete(resetToken);
    }

    private record SeedCategory(String name, TransactionType type, String color) {}
}
