package com.example.placementmanagementsystem.service;

import com.example.placementmanagementsystem.model.User;
import com.example.placementmanagementsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service class for Spring Security User Details
 * Implements UserDetailsService to allow Spring Security to load a user from the database when authenticating a user.
 */
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    /**
     * Load a User by their unique username
     *
     * @param username Unique username of the user to be loaded
     * @return User object that has the given username, if found. User object implements UserDetails interface, therefore Spring Security can use it to authenticate the user by their username and password.
     * @throws UsernameNotFoundException If the user with the given username is not found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + " not found");
        } else {
            return user;
        }
    }
}