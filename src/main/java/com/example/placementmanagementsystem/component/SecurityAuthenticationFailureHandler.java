package com.example.placementmanagementsystem.component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication failure handler
 */
@Component
public class SecurityAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Handles login failure and redirects to the login page with a relevant error message in the URL parameter 'error'
     *
     * @param request   The HTTP request
     * @param response  The HTTP response
     * @param exception The exception that caused the failure
     * @throws IOException      If an error occurs during redirection
     * @throws ServletException If an error occurs during redirection
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof AccountExpiredException) { // accountNonExpired == false
            response.sendRedirect("/login?error=accountExpired");
        } else if (exception instanceof LockedException) { // accountNonLocked == false
            response.sendRedirect("/login?error=locked");
        } else if (exception instanceof CredentialsExpiredException) { // credentialsNonExpired == false
            response.sendRedirect("/login?error=credentialsExpired");
        } else if (exception instanceof DisabledException) { // enabled == false
            response.sendRedirect("/login?error=disabled");
        } else { // Other errors
            response.sendRedirect("/login?error=true");
        }

        System.out.println("Authentication failed: " + exception.getMessage());
    }
}