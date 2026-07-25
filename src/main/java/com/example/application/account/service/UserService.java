package com.example.application.account.service;

import com.example.application.account.model.AppUser;
import com.example.application.account.repository.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean userExists(String username) {
        return userRepo.findByUsername(username) != null;
    }

    public AppUser getById(Long userId) {
        return userId == null ? null : userRepo.findById(userId).orElse(null);
    }

    public AppUser getByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}
