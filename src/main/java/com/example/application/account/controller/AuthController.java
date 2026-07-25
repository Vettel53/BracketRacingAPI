package com.example.application.account.controller;

import com.example.application.account.dto.AuthResponse;
import com.example.application.account.dto.LoginRequest;
import com.example.application.account.dto.RegisterRequest;
import com.example.application.account.model.AppUser;
import com.example.application.account.service.AccountCreationService;
import com.example.application.account.service.AuthService;
import com.example.application.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountCreationService accountCreationService;
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AccountCreationService accountCreationService, AuthService authService, JwtService jwtService) {
        this.accountCreationService = accountCreationService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = accountCreationService.createUser(request.username(), request.password());
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
