package com.ukhanov.bigtik.core.config;

import com.ukhanov.bigtik.core.model.Role;
import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN);
                userRepository.save(admin);
            }
        };
    }
}