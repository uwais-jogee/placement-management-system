package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.EmailToken;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for EmailToken entity
 */
@Repository
public interface EmailTokenRepo extends CrudRepository<EmailToken, String> {

    /**
     * Query to find EmailToken by the given unique token
     *
     * @param token The token to search for
     * @return The EmailToken object if found, null otherwise
     */
    Optional<EmailToken> findByToken(String token);
}