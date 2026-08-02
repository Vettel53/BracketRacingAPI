package com.example.application.account.service;

import com.example.application.account.model.AppUser;
import com.example.application.account.repository.UserRepo;
import com.example.application.security.JwtService;
import com.example.application.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(String username, String rawPassword) {
        AppUser user = userRepo.findByUsername(username);

        if (user == null || !passwordEncoder.matches(rawPassword, user.getHashedPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid username or password");
        }

        return jwtService.generateToken(user.getId(), user.getUsername());
    }
}
