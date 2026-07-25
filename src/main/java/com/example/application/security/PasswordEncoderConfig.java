package com.example.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * We depend on spring-security-crypto only (for BCrypt) - not the full
 * spring-boot-starter-security. That means no filter chain, no UserDetailsService,
 * no autoconfigured PasswordEncoder bean, so it's declared explicitly here.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
