package com.ukhanov.bigtik.core.exception;

import com.ukhanov.bigtik.core.service.IpAddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, Model model, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String clientIp = ipAddressService.getClientIpAddress(request);
        
        logger.warn("File upload size exceeded [{}] - IP: {}, URI: {}, Message: {}", 
                errorId,
                clientIp, 
                request.getRequestURI(), 
                ex.getMessage());
        
        model.addAttribute("errorId", errorId);
        model.addAttribute("errorTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("errorIp", clientIp);
        
        return new ModelAndView("error");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        logger.warn("Access denied [{}] - IP: {}, URI: {}, Message: {}", 
                errorId,
                ipAddressService.getClientIpAddress(request), 
                request.getRequestURI(), 
                ex.getMessage());
        
        return new ModelAndView("redirect:/error?accessdenied");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ModelAndView handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        logger.warn("Authentication error [{}] - IP: {}, URI: {}, Message: {}", 
                errorId,
                ipAddressService.getClientIpAddress(request), 
                request.getRequestURI(), 
                ex.getMessage());
        
        return new ModelAndView("redirect:/login?error");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        logger.error("Data integrity violation [{}] - IP: {}, URI: {}, Message: {}", 
                errorId,
                ipAddressService.getClientIpAddress(request), 
                request.getRequestURI(), 
                ex.getMessage(), ex);
        
        return new ModelAndView("error");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ModelAndView handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        logger.warn("Bad credentials - IP: {}, Message: {}", 
                ipAddressService.getClientIpAddress(request), 
                ex.getMessage());
        
        return new ModelAndView("redirect:/login?error");
    }
}