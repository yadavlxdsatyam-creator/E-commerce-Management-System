package com.shopease.backend.service;

import com.shopease.backend.dto.AuthResponse;
import com.shopease.backend.dto.LoginRequest;
import com.shopease.backend.dto.RegisterRequest;
import com.shopease.backend.model.User;
import com.shopease.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (request == null) throw new RuntimeException("Registration data is required");

        String name = request.getName() == null ? "" : request.getName().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String phone = request.getPhone() == null ? "" : request.getPhone().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (name.isBlank()) throw new RuntimeException("Name is required");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new RuntimeException("Enter a valid email address");
        if (!phone.matches("^[0-9]{10}$"))
            throw new RuntimeException("Enter a valid 10-digit phone number");
        if (password.length() < 6)
            throw new RuntimeException("Password must be at least 6 characters");
        if (userRepository.emailExists(email))
            throw new RuntimeException("Email already registered");

        int userId = userRepository.createUser(
                name, email, passwordEncoder.encode(password), "CUSTOMER"
        );
        userRepository.createCustomer(userId, phone);
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null) throw new RuntimeException("Login data is required");

        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        User user = userRepository.findByEmail(email);

        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        int customerId = "CUSTOMER".equals(user.getRole())
                ? userRepository.findCustomerId(user.getUserId())
                : 0;

        return new AuthResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                customerId
        );
    }
}
