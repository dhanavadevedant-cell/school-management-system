package com.example.demo.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StudentEventListener {

    @KafkaListener(topics = "student-topic", groupId = "school-group")
    public void consume(String message) {
        System.out.println("LOG FROM KAFKA: " + message);
    }
}