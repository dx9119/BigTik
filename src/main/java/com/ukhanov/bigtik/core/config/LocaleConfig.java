package com.ukhanov.bigtik.core.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    private static final String COOKIE_NAME = "BIGTIK_LOCALE";

    @Bean
    public LocaleResolver localeResolver() {
        return new PersistentLocaleResolver();
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    private static class PersistentLocaleResolver implements LocaleResolver {
        
        @Override
        public Locale resolveLocale(HttpServletRequest request) {
            String langParam = request.getParameter("lang");
            
            if (langParam != null) {
                if (langParam.isEmpty() || "ru".equals(langParam)) {
                    return Locale.ROOT;
                } else if ("en".equals(langParam)) {
                    return Locale.ENGLISH;
                }
            }
            
            Cookie cookie = findCookie(request);
            if (cookie != null) {
                String value = cookie.getValue();
                if (value == null || value.isEmpty() || "ru".equals(value)) {
                    return Locale.ROOT;
                } else if ("en".equals(value)) {
                    return Locale.ENGLISH;
                }
            }
            
            return Locale.ROOT;
        }

        @Override
        public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
            String value;
            if (locale == null || locale.equals(Locale.ROOT)) {
                value = "";
            } else if (locale.equals(Locale.ENGLISH)) {
                value = "en";
            } else {
                value = "";
            }
            
            Cookie cookie = new Cookie(COOKIE_NAME, value);
            cookie.setPath("/");
            cookie.setMaxAge(365 * 24 * 60 * 60);
            response.addCookie(cookie);
        }

        private Cookie findCookie(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (COOKIE_NAME.equals(cookie.getName())) {
                        return cookie;
                    }
                }
            }
            return null;
        }
    }
}