package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.Admin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for Admin entity
 */
@Repository
public interface AdminRepo extends CrudRepository<Admin, String> {

    /**
     * Query to find Admin by the given unique username
     *
     * @param username The username to search for
     * @return The Admin object if found, null otherwise
     */
    Admin findAdminByUsername(String username);
}