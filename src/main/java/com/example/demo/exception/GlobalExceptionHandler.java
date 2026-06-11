package com.example.demo.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String AUDIT_TOPIC = "school-audit-logs";

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        
        // 1. Identify who tried to break the rules
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Prepare the Security Alert message for Kafka
        String alertMessage = String.format("🚨 SECURITY VIOLATION | USER: %s attempted an unauthorized action!", username);
        
        // 3. Send to Admin's Audit Log topic
        kafkaTemplate.send(AUDIT_TOPIC, alertMessage);

        // 4. Create the professional JSON response for Postman
        Map<String, Object> response = new HashMap<>();
        response.put("status", 403);
        response.put("error", "Forbidden");
        response.put("message", "Access Denied: You do not have permission for this resource.");
        response.put("detail", "This incident has been reported to the Admin.");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}