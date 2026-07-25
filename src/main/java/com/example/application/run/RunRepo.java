package com.example.application.run;

import com.example.application.account.model.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RunRepo extends JpaRepository<Run, Long> {
    List<Run> findByAppUser(AppUser appUser);
}
