package com.example.demo.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StudentConsumer {

    // This method "listens" to the topic and runs every time a message arrives
    @KafkaListener(topics = "student-topic", groupId = "school-mgmt-group")
    public void listen(String message) {
        System.out.println("----------------------------------------------");
        System.out.println("KAFKA LISTENER: Received new student data!");
        System.out.println("Payload: " + message);
        System.out.println("----------------------------------------------");
        
        // This is where you would trigger an email, 
        // generate a PDF report, or update a dashboard.
    }
}