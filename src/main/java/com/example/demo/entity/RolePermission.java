package com.example.demo.entity; 

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_permissions")
public class RolePermission {
    @Id
    private String roleName; // This will store "ADMIN", "TEACHER", or "STUDENT"
    private boolean canCreate;
    private boolean canUpdate;
    private boolean canDelete;

    // Default constructor (Required by Hibernate)
    public RolePermission() {}

    public RolePermission(String roleName, boolean canCreate, boolean canUpdate, boolean canDelete) {
        this.roleName = roleName;
        this.canCreate = canCreate;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
    }

    // Getters and Setters
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public boolean isCanCreate() { return canCreate; }
    public void setCanCreate(boolean canCreate) { this.canCreate = canCreate; }
    public boolean isCanUpdate() { return canUpdate; }
    public void setCanUpdate(boolean canUpdate) { this.canUpdate = canUpdate; }
    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }
}