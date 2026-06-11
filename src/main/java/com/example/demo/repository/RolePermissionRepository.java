package com.example.demo.repository;

import com.example.demo.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    // This allows you to do permissionRepository.findById("ADMIN")
}