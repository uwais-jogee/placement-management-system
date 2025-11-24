package com.example.placementmanagementsystem.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * Controller class for handling global error endpoints across the application
 */
@Controller
public class GlobalErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public GlobalErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Error page endpoint, to map any errors to errorPage.html, with the error details to be displayed
     *
     * @param webRequest The web request object to get the error details from
     * @param model      The model object to add the error details to
     * @return The error page template
     */
    @GetMapping("/error")
    public String handleError(WebRequest webRequest, Model model) {
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        model.addAttribute("errorCode", errorAttributes.get("status"));
        model.addAttribute("errorMessage", errorAttributes.get("error"));
        model.addAttribute("errorDetails", errorAttributes.get("message"));
        model.addAttribute("path", errorAttributes.get("path"));

        return "errorPage"; // Maps to errorPage.html
    }

    /**
     * Access denied endpoint, to map spring security access denied errors to errorPage.html
     *
     * @param model The model object to add the error details to
     * @return The error page template
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "Access Denied");
        System.out.println("Raised by accessDenied | Error: 403 | Message: Access Denied");
        return "errorPage";
    }
}