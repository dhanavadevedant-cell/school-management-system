package com.example.demo.repository;

import com.example.demo.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    // Allows students to instantly filter materials by subject name
    List<Resource> findBySubjectContainingIgnoreCase(String subject);
}