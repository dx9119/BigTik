package com.ukhanov.bigtik.core.service;

import com.ukhanov.bigtik.core.model.Role;
import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.removeIf(r -> r != Role.ADMIN);
        roles.add(newRole);

        user.setRoles(roles);
        userRepository.save(user);
        logger.info("Changed role for user '{}' to {}", user.getUsername(), newRole);
    }

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRoles().contains(Role.ADMIN)) {
            throw new IllegalArgumentException("Cannot block admin user");
        }

        user.setEnabled(false);
        userRepository.save(user);
        logger.info("Blocked user: {}", user.getUsername());
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        logger.info("Unblocked user: {}", user.getUsername());
    }

    public void deleteUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getUsername().equals(adminUsername)) {
            throw new IllegalArgumentException("Cannot delete yourself");
        }

        userRepository.delete(user);
        logger.info("Deleted user: {} by admin: {}", user.getUsername(), adminUsername);
    }
}
