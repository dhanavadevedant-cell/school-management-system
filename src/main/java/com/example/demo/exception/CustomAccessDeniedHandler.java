package com.example.demo.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                       AccessDeniedException accessDeniedException) throws IOException {
        
        // 1. Get the username from Keycloak token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Prepare the Kafka alert
        String alertMessage = String.format("🚨 SECURITY VIOLATION | USER: %s attempted an unauthorized action!", username);
        
        // 3. Send to Kafka
        kafkaTemplate.send("school-audit-logs", alertMessage);

        // 4. Send the 403 response back to Postman/Frontend
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"Incident reported to Admin.\"}");
    }
}