package com.ukhanov.bigtik.core.controller;

import com.ukhanov.bigtik.core.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private final AuthService authService;

    public PageController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            authService.register(username, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/home")
    public String home(org.springframework.security.core.Authentication authentication, 
                      Model model, java.util.Locale locale) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        } else {
            model.addAttribute("username", "Гость");
            model.addAttribute("isAdmin", false);
        }
        return "home";
    }
}