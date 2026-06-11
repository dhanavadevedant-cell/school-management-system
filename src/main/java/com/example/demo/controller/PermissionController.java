package com.example.demo.controller; // Ensure this matches your package structure

import com.example.demo.entity.RolePermission;
import com.example.demo.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allows React to call these APIs
public class PermissionController {

    @Autowired
    private RolePermissionRepository permissionRepository;

    // 1. GET /api/permissions -> Loads permissions for React
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Map<String, Boolean>>> getPermissions() {
        List<RolePermission> permissionsList = permissionRepository.findAll();
        Map<String, Map<String, Boolean>> response = new HashMap<>();

        for (RolePermission perm : permissionsList) {
            Map<String, Boolean> actionMap = new HashMap<>();
            actionMap.put("canCreate", perm.isCanCreate());
            actionMap.put("canUpdate", perm.isCanUpdate());
            actionMap.put("canDelete", perm.isCanDelete());
            response.put(perm.getRoleName(), actionMap);
        }

        return ResponseEntity.ok(response);
    }

    // 2. PUT /api/permissions/update -> Saves the Admin's changed checkboxes to Postgres
    @PutMapping("/permissions/update")
    public ResponseEntity<?> updatePermissions(@RequestBody Map<String, Map<String, Boolean>> payload) {
        for (Map.Entry<String, Map<String, Boolean>> entry : payload.entrySet()) {
            String roleName = entry.getKey();
            Map<String, Boolean> actions = entry.getValue();

            RolePermission permission = permissionRepository.findById(roleName)
                    .orElse(new RolePermission());
            
            permission.setRoleName(roleName);
            permission.setCanCreate(actions.getOrDefault("canCreate", false));
            permission.setCanUpdate(actions.getOrDefault("canUpdate", false));
            permission.setCanDelete(actions.getOrDefault("canDelete", false));

            permissionRepository.save(permission);
        }
        return ResponseEntity.ok("Permissions updated successfully!");
    }
}