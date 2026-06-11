package com.example.demo.controller;

import com.example.demo.entity.Teacher;
import com.example.demo.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TeacherRepository teacherRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // Constructor Injection is the best practice for adding dependencies
    @Autowired
    public AdminController(TeacherRepository teacherRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.teacherRepository = teacherRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // UPDATED: Now actually removes from DB and logs to Kafka
    @DeleteMapping("/remove-teacher/{id}")
    public String removeTeacher(@PathVariable Long id) {
        if (teacherRepository.existsById(id)) {
            // 1. Delete from PostgreSQL
            teacherRepository.deleteById(id);

            // 2. Send Audit Log to Kafka
            String auditMessage = "AUDIT_LOG: Admin deleted teacher with ID " + id;
            kafkaTemplate.send("school-audit-logs", auditMessage);

            return "Admin successfully removed Teacher with ID: " + id + " (Incident logged to Kafka)";
        } else {
            return "Error: Teacher with ID " + id + " not found in database.";
        }
    }

    @GetMapping("/staff-summary")
    public String getStaffSummary() {
        // You can now use the repository to get real counts!
        long count = teacherRepository.count();
        return "System Summary: " + count + " Teachers active in the database.";
    }

    @PostMapping("/system-maintenance")
    public String performMaintenance() {
        kafkaTemplate.send("school-audit-logs", "SYSTEM_EVENT: Maintenance mode activated.");
        return "System maintenance mode activated by Admin.";
    }
}