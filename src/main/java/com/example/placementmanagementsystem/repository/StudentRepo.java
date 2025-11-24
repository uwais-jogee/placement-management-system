package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for Student entity
 */
@Repository
public interface StudentRepo extends CrudRepository<Student, String> {

    /**
     * Query to find student by their unqiue username
     *
     * @param username Unique username of student to search for
     * @return Student object if found, null otherwise
     */
    Student findStudentByUsername(String username);
}