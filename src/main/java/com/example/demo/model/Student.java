package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    // This tells Postgres to use the column name 'roll_no' 
    // while your Java code continues to use 'studentRollNumber'
    @Column(name = "roll_no")
    private String studentRollNumber;

    // Standard Constructor (Required by JPA)
    public Student() {}

    public Student(String name, String email, String studentRollNumber) {
        this.name = name;
        this.email = email;
        this.studentRollNumber = studentRollNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }
}