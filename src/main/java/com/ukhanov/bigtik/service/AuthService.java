package com.ukhanov.bigtik.service;

import com.ukhanov.bigtik.model.Role;
import com.ukhanov.bigtik.model.User;
import com.ukhanov.bigtik.repository.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

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
        return userRepository.save(user);
    }
}