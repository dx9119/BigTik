package com.ukhanov.bigtik.core.service;

import com.ukhanov.bigtik.core.model.Role;
import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("register.error", null, locale);
            throw new IllegalArgumentException(message);
        }
        User user = new User(username, passwordEncoder.encode(password), Role.USER);
        User saved = userRepository.save(user);
        logger.info("Registered new user: {}", username);
        return saved;
    }

    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password updated for user: {}", user.getUsername());
    }
}