package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.Tutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for Tutor entity
 */
@Repository
public interface TutorRepo extends CrudRepository<Tutor, String> {

    /**
     * Query to find tutor by their unique username
     *
     * @param username Unique username of the tutor to search for
     * @return The tutor object with the given username if found, null otherwise
     */
    Tutor findTutorByUsername(String username);
}