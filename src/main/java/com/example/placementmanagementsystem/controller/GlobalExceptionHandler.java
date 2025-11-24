package com.example.placementmanagementsystem.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;

/**
 * Controller advice class to handle exceptions across the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles AccessDeniedException, displays a 403 error page and logs the error in the console
     *
     * @param ex    AccessDeniedException to be handled
     * @param model The model to add attributes to
     * @return The error page view
     */
    @ExceptionHandler(AccessDeniedException.class)
    // This does not handle 403 error from Spring Security, as it is handled in GlobalErrorController
    public String handleAccessDeniedException(AccessDeniedException ex, Model model) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "Access Denied: " + ex.getMessage());
        System.out.println("Raised by handleAccessDeniedException | Error: 403 | Message: Access Denied: " + ex.getMessage());
        return "errorPage";
    }

    /**
     * Handles ResponseStatusException, displays the error page with the status code and message and logs the error in the console
     *
     * @param ex    ResponseStatusException to be handled
     * @param model The model to add attributes to
     * @return The error page view
     */
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model) {
        model.addAttribute("errorCode", ex.getStatusCode().value());
        model.addAttribute("errorMessage", ex.getReason());
        System.out.println("Raised by handleResponseStatusException | Error: " + ex.getStatusCode().value() + " | Message: " + ex.getReason());
        return "errorPage";
    }

    /**
     * Handles generic server error exceptions, displays a 500 error page and logs the error in the console
     *
     * @param ex    Exception to be handled
     * @param model The model to add attributes to
     * @return The error page view
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        System.out.println("Raised by handleGenericException | Error: 500 | Message: An unexpected error occurred: " + ex.getMessage());
        return "errorPage";
    }
}