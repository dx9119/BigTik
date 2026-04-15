package com.ukhanov.bigtik.core.controller;

import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import com.ukhanov.bigtik.core.security.CustomUserDetails;
import com.ukhanov.bigtik.core.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final MessageSource messageSource;

    public ProfileController(UserRepository userRepository, AuthService authService, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.messageSource = messageSource;
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

    @PostMapping("/profile/password")
    public String updatePassword(@RequestParam String currentPassword,
                          @RequestParam String newPassword,
                          @RequestParam String confirmPassword,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          RedirectAttributes redirectAttributes,
                          Authentication authentication) {
        Locale locale = LocaleContextHolder.getLocale();
        
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        if (newPassword == null || newPassword.length() < 4) {
            String msg = messageSource.getMessage("profile.password.error.short", null, locale);
            redirectAttributes.addFlashAttribute("error", msg);
            return "redirect:/profile";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            String msg = messageSource.getMessage("profile.password.error.mismatch", null, locale);
            redirectAttributes.addFlashAttribute("error", msg);
            return "redirect:/profile";
        }
        
        try {
            authService.updatePassword(userDetails.getId(), currentPassword, newPassword);
            String msg = messageSource.getMessage("profile.password.success", null, locale);
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (IllegalArgumentException e) {
            String key = e.getMessage().contains("incorrect") ? "profile.password.error.wrong" : "profile.password.error";
            String msg = messageSource.getMessage(key, null, locale);
            redirectAttributes.addFlashAttribute("error", msg);
        }
        
        return "redirect:/profile";
    }
}