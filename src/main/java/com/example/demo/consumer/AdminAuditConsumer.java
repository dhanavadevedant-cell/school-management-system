package com.example.demo.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AdminAuditConsumer {

    /**
     * This listener acts as the "Admin's Dashboard".
     * It specifically listens to the audit topic to track actions 
     * performed by Teachers and Admins.
     */
    @KafkaListener(topics = "school-audit-logs", groupId = "admin-audit-group")
    public void consumeAuditLog(String message) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🚨 [ADMIN AUDIT ALERT]");
        System.out.println("LOG DETAIL: " + message);
        System.out.println("=".repeat(50) + "\n");
    }

    /**
     * You can keep your original student notification listener here too
     * or in a separate file.
     */
    @KafkaListener(topics = "student-topic", groupId = "student-group")
    public void consumeStudentUpdates(String message) {
        System.out.println("📩 [NOTIFICATION]: " + message);
    }
}