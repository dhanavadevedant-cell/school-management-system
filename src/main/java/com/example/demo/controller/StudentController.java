package com.example.demo.controller;

import com.example.demo.model.Student;
import com.example.demo.entity.RolePermission;
import com.example.demo.repository.RolePermissionRepository;
import com.example.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RolePermissionRepository permissionRepository;

    /**
     * DATABASE-DRIVEN PERMISSIONS:
     * Logic updated to filter for specific app roles and default to all FALSE.
     */
    @GetMapping("/permissions")
    public RolePermission getUserPermissions(Authentication authentication) {
        if (authentication == null) {
            return new RolePermission("ANONYMOUS", false, false, false);
        }

        // Search through all authorities for TEACHER or ADMIN
        Optional<String> activeRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                // Filter to ensure we only pick up roles defined in your DB
                .filter(role -> role.equals("ADMIN") || role.equals("TEACHER"))
                .findFirst();

        String roleName = activeRole.orElse("GUEST");

        // Fetch from DB. If not found, default to false/false/false to stay secure.
        return permissionRepository.findById(roleName)
                .orElse(new RolePermission(roleName, false, false, false));
    }

    @PostMapping("/permissions/update")
    @PreAuthorize("hasRole('ADMIN')")
    public RolePermission updatePermissions(@RequestBody RolePermission newPerms) {
        kafkaTemplate.send("student-audit", "SYSTEM: Permissions updated for role " + newPerms.getRoleName());
        return permissionRepository.save(newPerms);
    }

    @PostMapping("/add")
    public Student addStudent(@RequestBody Student student, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";
        Student savedStudent = studentService.saveStudent(student);
        kafkaTemplate.send("student-audit", "CREATE: " + username + " added " + savedStudent.getName());
        return savedStudent;
    }

    @GetMapping("/all")
    public List<Student> getStudents() {
        return studentService.getAllStudents();
    }

    @PutMapping("/update/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student studentDetails, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";
        Student updatedStudent = studentService.updateStudent(id, studentDetails);
        kafkaTemplate.send("student-audit", "UPDATE: " + username + " modified ID " + id);
        return updatedStudent;
    }

    @DeleteMapping("/delete/{id}")
    public void deleteStudent(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";
        kafkaTemplate.send("student-audit", "DELETE: " + username + " removed ID " + id);
        studentService.deleteStudent(id);
    }
}