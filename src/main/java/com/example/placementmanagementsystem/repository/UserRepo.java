package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.enumeration.Role;
import com.example.placementmanagementsystem.model.EmailToken;
import com.example.placementmanagementsystem.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository for User entity
 */
@Repository
public interface UserRepo extends CrudRepository<User, String> {

    /**
     * Query to find a User by their unique username
     *
     * @param username Unique username of the User to search for
     * @return The User object with the given username if found, null otherwise
     */
    User findUserByUsername(String username);

    /**
     * Query to find all users with a given role
     *
     * @param role The role to filter by
     * @return List of users with the given role
     */
    List<User> findUsersByRole(Role role);

    /**
     * Query to find all users with one of the given roles
     *
     * @param roles List of roles to filter by
     * @return List of users with one of the given roles
     */
    List<User> findUsersByRoleIn(List<Role> roles);

    /**
     * Query to find a User by their unique email
     *
     * @param email Unique email of the User to search for
     * @return The User object with the given email if found, null otherwise
     */
    User findUserByEmail(String email);

    /**
     * Query to find a User by a given password reset email token
     *
     * @param emailToken The password reset email token to search for
     * @return The User object with the given password reset email token if found, null otherwise
     */
    User findUserByPasswordResetEmailToken(EmailToken emailToken);

    /**
     * Query to find a User by a given account activation email token
     *
     * @param emailToken The account activation email token to search for
     * @return The User object with the given account activation email token if found, null otherwise
     */
    User findUserByAccountActivationEmailToken(EmailToken emailToken);
}