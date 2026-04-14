package com.ukhanov.bigtik.core.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LanguageController {

    @GetMapping("/lang")
    public String changeLanguage(@RequestParam String lang, 
                                  @RequestParam(required = false) String redirect,
                                  HttpServletResponse response) {
        Cookie cookie = new Cookie("locale", lang);
        cookie.setMaxAge(31536000);
        cookie.setPath("/");
        response.addCookie(cookie);
        
        String targetUrl = "/login";
        if (redirect != null && !redirect.isEmpty()) {
            try {
                targetUrl = URLDecoder.decode(redirect, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                targetUrl = redirect;
            }
        }
        
        if (!targetUrl.startsWith("/")) {
            targetUrl = "/" + targetUrl;
        }
        
        return "redirect:" + targetUrl;
    }
}