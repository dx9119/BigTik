package com.ukhanov.bigtik.core.controller;

import com.ukhanov.bigtik.core.model.Role;
import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public String manageUsers(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {
        Page<User> usersPage = adminService.getAllUsers(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username")));

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("roles", Role.values());

        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam String role,
                             @RequestParam int page,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            adminService.changeRole(id, Role.valueOf(role));
            redirectAttributes.addFlashAttribute("success", "admin.role.changed");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users?page=" + page;
    }

    @PostMapping("/users/{id}/block")
    public String blockUser(@PathVariable Long id,
                            @RequestParam int page,
                            RedirectAttributes redirectAttributes) {
        try {
            adminService.blockUser(id);
            redirectAttributes.addFlashAttribute("success", "admin.user.blocked.success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users?page=" + page;
    }

    @PostMapping("/users/{id}/unblock")
    public String unblockUser(@PathVariable Long id,
                              @RequestParam int page,
                              RedirectAttributes redirectAttributes) {
        try {
            adminService.unblockUser(id);
            redirectAttributes.addFlashAttribute("success", "admin.user.unblocked");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users?page=" + page;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @RequestParam int page,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "admin.user.deleted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users?page=" + page;
    }
}
