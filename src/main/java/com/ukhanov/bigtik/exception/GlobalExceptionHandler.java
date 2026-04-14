package com.ukhanov.bigtik.exception;

import com.ukhanov.bigtik.service.IpAddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final IpAddressService ipAddressService;

    public GlobalExceptionHandler(IpAddressService ipAddressService) {
        this.ipAddressService = ipAddressService;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, Model model, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String clientIp = ipAddressService.getClientIpAddress(request);
        
        logger.error("Error [{}] - IP: {}, URI: {}, Message: {}", 
                errorId,
                clientIp, 
                request.getRequestURI(), 
                ex.getMessage(), ex);
        
        model.addAttribute("errorId", errorId);
        model.addAttribute("errorTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("errorIp", clientIp);
        
        return new ModelAndView("error");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex, Model model, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String clientIp = ipAddressService.getClientIpAddress(request);
        
        logger.warn("Validation error [{}] - IP: {}, URI: {}, Message: {}", 
                errorId,
                clientIp, 
                request.getRequestURI(), 
                ex.getMessage());
        
        model.addAttribute("errorId", errorId);
        model.addAttribute("errorTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("errorIp", clientIp);
        
        return new ModelAndView("error");
    }
}