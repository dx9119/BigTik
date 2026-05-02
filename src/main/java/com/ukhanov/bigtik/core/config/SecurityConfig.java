package com.ukhanov.bigtik.core.config;

import com.ukhanov.bigtik.core.filter.MDCLoggingFilter;
import com.ukhanov.bigtik.core.service.IpAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final IpAddressService ipAddressService;

    public SecurityConfig(IpAddressService ipAddressService) {
        this.ipAddressService = ipAddressService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/login", "/register", "/lang", "/set-lang", "/error/**", "/css/**", "/js/**", "/images/**", "/home").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/video/upload/**").hasAnyRole("ADMIN", "CREATOR")
                .requestMatchers("/video/*/delete").hasAnyRole("ADMIN", "CREATOR")
                .requestMatchers("/video/list").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(this::handleAccessDenied)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .addFilterBefore(mdcLoggingFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                     org.springframework.security.access.AccessDeniedException ex) throws IOException {
        response.sendRedirect("/error/access-denied");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MDCLoggingFilter mdcLoggingFilter() {
        return new MDCLoggingFilter(ipAddressService);
    }
}
