package com.example.application.account.service;

import com.example.application.account.model.AppUser;
import com.example.application.account.repository.UserRepo;
import com.example.application.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AppUser + UserRepo is now the single source of truth for accounts. Previously
 * this also wrote a parallel copy of the user into Spring Security's
 * UserDetailsManager, which meant two independently-hashed passwords for the
 * same account that could drift out of sync. That path is gone now that auth
 * is self-hosted JWT instead of Spring Security.
 */
@Service
public class AccountCreationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public AccountCreationService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser createUser(String username, String rawPassword) {
        if (userRepo.findByUsername(username) != null) {
            throw new BusinessException("Username '" + username + "' is already taken");
        }

        AppUser newUser = new AppUser(username, passwordEncoder.encode(rawPassword));
        return userRepo.save(newUser);
    }
}
