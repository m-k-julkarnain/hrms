package com.dbms.hrms.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex, Model model, HttpServletRequest req) {
        model.addAttribute("error", "Operation failed due to data constraint (duplicate or invalid data).");
        model.addAttribute("message", ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage());
        model.addAttribute("path", req.getRequestURI());
        return "error/custom-dberror"; // create a simple Thymeleaf template for this
    }

    // optionally handle generic Exception to avoid white-label pages
    @ExceptionHandler(Exception.class)
    public String handleAll(Exception ex, Model model, HttpServletRequest req) {
        model.addAttribute("error", "Unexpected error occurred.");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", req.getRequestURI());
        return "error/general";
    }
}
