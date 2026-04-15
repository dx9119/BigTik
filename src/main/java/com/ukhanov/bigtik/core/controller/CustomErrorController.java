package com.ukhanov.bigtik.core.controller;

import com.ukhanov.bigtik.core.service.IpAddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class CustomErrorController {

    private final IpAddressService ipAddressService;

    public CustomErrorController(IpAddressService ipAddressService) {
        this.ipAddressService = ipAddressService;
    }

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        model.addAttribute("errorTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("errorIp", ipAddressService.getClientIpAddress(request));

        if (statusCode != null) {
            model.addAttribute("httpStatus", statusCode);

            if (statusCode == 404) {
                return "error/404";
            }
        }

        return "error";
    }

    @GetMapping("/error/access-denied")
    public String accessDenied(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("errorTime")) {
            model.addAttribute("errorTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (!model.containsAttribute("errorIp")) {
            model.addAttribute("errorIp", ipAddressService.getClientIpAddress(request));
        }
        return "error/access-denied";
    }
}
