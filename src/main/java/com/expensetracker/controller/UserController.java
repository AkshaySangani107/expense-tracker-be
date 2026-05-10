package com.expensetracker.controller;

import com.expensetracker.dto.AuthDtos.UserResponse;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.CurrentUser;
import com.expensetracker.service.Mapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/me")
    public UserResponse updateProfile(@AuthenticationPrincipal CurrentUser currentUser, @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findById(currentUser.id()).orElseThrow();
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        return Mapper.user(userRepository.save(user));
    }

    public record UpdateProfileRequest(String name) {}
}
