package com.ukhanov.bigtik.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LanguageController {

    @GetMapping("/set-lang")
    public String setLanguage(@RequestParam String lang, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null && !referer.isEmpty()) ? referer : "/home";
        
        if (redirectUrl.contains("?")) {
            redirectUrl += "&lang=" + lang;
        } else {
            redirectUrl += "?lang=" + lang;
        }
        
        return "redirect:" + redirectUrl;
    }
}