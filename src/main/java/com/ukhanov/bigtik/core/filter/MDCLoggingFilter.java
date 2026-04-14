package com.ukhanov.bigtik.core.filter;

import com.ukhanov.bigtik.core.service.IpAddressService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class MDCLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MDCLoggingFilter.class);

    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String SESSION_ID = "sessionId";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String REQUEST_URI = "requestUri";
    public static final String HTTP_METHOD = "httpMethod";

    private final IpAddressService ipAddressService;

    public MDCLoggingFilter(IpAddressService ipAddressService) {
        this.ipAddressService = ipAddressService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            MDC.put(REQUEST_ID, UUID.randomUUID().toString().substring(0, 8));
            MDC.put(IP_ADDRESS, ipAddressService.getClientIpAddress(request));
            MDC.put(REQUEST_URI, request.getRequestURI());
            MDC.put(HTTP_METHOD, request.getMethod());
            
            String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : "no-session";
            MDC.put(SESSION_ID, sessionId);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String userName = null;
                if (auth.getPrincipal() instanceof com.ukhanov.bigtik.core.security.CustomUserDetails customUserDetails) {
                    userName = customUserDetails.getUsername();
                } else {
                    userName = auth.getName();
                }
                
                if (userName != null) {
                    MDC.put(USER_NAME, userName);
                } else {
                    MDC.put(USER_NAME, "unknown");
                }
                
                if (auth.getDetails() instanceof org.springframework.security.web.authentication.WebAuthenticationDetails details) {
                    MDC.put(SESSION_ID, details.getSessionId());
                }
                logger.info("Request started - user authenticated");
            } else {
                MDC.put(USER_NAME, "anonymous");
                logger.info("Request started - anonymous user");
            }

            filterChain.doFilter(request, response);
        } finally {
            logger.info("Request completed");
            MDC.clear();
        }
    }
}