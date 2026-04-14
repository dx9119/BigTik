package com.ukhanov.bigtik.core.controller;

import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import com.ukhanov.bigtik.core.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication, 
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          HttpSession httpSession) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("authorities", authentication.getAuthorities());
        model.addAttribute("isAuthenticated", authentication.isAuthenticated());
        
        model.addAttribute("authDetailsAuthenticated", authentication.isAuthenticated());
        model.addAttribute("authDetailsSessionId", httpSession.getId());
        
        if (userDetails != null) {
            model.addAttribute("userId", userDetails.getId());
            
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        
        model.addAttribute("sessionId", httpSession.getId());
        model.addAttribute("sessionCreated", httpSession.getCreationTime());
        model.addAttribute("sessionLastAccessed", httpSession.getLastAccessedTime());
        
        return "profile";
    }
}