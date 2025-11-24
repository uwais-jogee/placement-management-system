package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.Address;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for Address entity
 */
@Repository
public interface AddressRepo extends CrudRepository<Address, String> {
}