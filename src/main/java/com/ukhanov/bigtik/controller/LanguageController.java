package com.ukhanov.bigtik.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        
        String targetUrl = (redirect != null && !redirect.isEmpty()) ? redirect : "/login";
        if (!targetUrl.startsWith("/")) {
            targetUrl = "/" + targetUrl;
        }
        return "redirect:" + targetUrl;
    }
}