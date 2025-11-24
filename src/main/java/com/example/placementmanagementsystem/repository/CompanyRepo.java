package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.Company;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CRUD repository for Company entity
 */
@Repository
public interface CompanyRepo extends CrudRepository<Company, Long> {

    /**
     * Query to find Company by the given unique company id
     *
     * @param companyId The company id to search for
     * @return The Company object if found, null otherwise
     */
    Optional<Company> findCompanyById(Long companyId);

    /**
     * Query to find the first Company by the given company name
     *
     * @param companyName The company name to search for
     * @return The Company object if found, null otherwise
     */
    Company findCompanyByCompanyName(String companyName);
}