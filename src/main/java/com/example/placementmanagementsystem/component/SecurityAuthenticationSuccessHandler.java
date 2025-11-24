package com.example.placementmanagementsystem.component;

import com.example.placementmanagementsystem.model.User;
import com.example.placementmanagementsystem.repository.UserRepo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom authentication success handler
 */
@Component
public class SecurityAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepo userRepo;

    /**
     * Handles login success, updates the last login time and redirects to the root URL, which is the dashboard
     *
     * @param request        The HTTP request
     * @param response       The HTTP response
     * @param authentication The authentication object
     * @throws IOException      If an error occurs during redirection
     * @throws ServletException If an error occurs during redirection
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        System.out.println("Authentication success: " + username);
        User user = userRepo.findUserByUsername(username);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepo.save(user);
            response.sendRedirect("/");
        }
    }
}