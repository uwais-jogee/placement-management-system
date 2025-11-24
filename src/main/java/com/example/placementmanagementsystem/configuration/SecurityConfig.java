package com.example.placementmanagementsystem.configuration;

import com.example.placementmanagementsystem.component.SecurityAuthenticationFailureHandler;
import com.example.placementmanagementsystem.component.SecurityAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration class
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityAuthenticationFailureHandler securityAuthenticationFailureHandler;
    @Autowired
    private SecurityAuthenticationSuccessHandler securityAuthenticationSuccessHandler;

    /**
     * Password encoder bean
     *
     * @return BCryptPasswordEncoder for encoding passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security filter chain bean for configuring Spring Security
     *
     * @param http HttpSecurity object
     * @return SecurityFilterChain object for configuring Spring Security
     * @throws Exception If an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "main.css", "favicon.ico").permitAll() // Ensure main.css is not blocked before logging in
                        .requestMatchers("/login","/forgot-password/**", "/reset-password/**","/activate-account/**").permitAll() // Allow access login endpoint without authentication
                        .requestMatchers("/company/**").permitAll() // Allow company to complete authorisation without logging in
                        .requestMatchers("/student/**").hasRole("STUDENT").requestMatchers("/tutor/**").hasRole("TUTOR").requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/user-manager/**").hasRole("USER_MANAGER").requestMatchers("/oauth2/**").permitAll() // Allow OAuth2 endpoints
                        .requestMatchers("/tutor/visits/new/calendar-auth/callback").authenticated() // Ensure calendar auth callback is authenticated
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin.loginPage("/login")
                        .successHandler(securityAuthenticationSuccessHandler)
                        .failureHandler(securityAuthenticationFailureHandler)
                        .permitAll())
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/oauth2/authorization/microsoft")
                        .defaultSuccessUrl("/tutor/visits/new"))
                .logout(logout -> logout.invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/access-denied"));
        return http.build();
    }
}