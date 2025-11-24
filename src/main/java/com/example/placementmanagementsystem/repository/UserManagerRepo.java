package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.UserManager;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for UserManager entity
 */
@Repository
public interface UserManagerRepo extends CrudRepository<UserManager, String> {

    /**
     * Query to find UserManager by their unique username
     *
     * @param username Unique username of the UserManager to search for
     * @return The UserManager object with the given username if found, null otherwise
     */
    UserManager findUserManagerByUsername(String username);
}