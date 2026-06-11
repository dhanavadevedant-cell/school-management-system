package com.example.demo.service;

import com.example.demo.model.Student;
import com.example.demo.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String STUDENT_TOPIC = "student-topic";
    private final String AUDIT_TOPIC = "school-audit-logs";

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "Anonymous_User"; 
    }

    public Student saveStudent(Student student) {
        Student savedStudent = studentRepository.save(student);
        String auditMessage = String.format("ACTION: CREATE | PERFORMED BY: %s | STUDENT: %s", 
                                            getCurrentUser(), savedStudent.getName());
        kafkaTemplate.send(AUDIT_TOPIC, auditMessage);
        
        String message = String.format("New Student Added: [ID: %d, Name: %s, Email: %s]", 
                                savedStudent.getId(), 
                                savedStudent.getName(), 
                                savedStudent.getEmail());
        kafkaTemplate.send(STUDENT_TOPIC, message);
        
        return savedStudent;
    }

    // --- ADDED THIS METHOD FOR THE UPDATE LOGIC ---
    public Student updateStudent(Long id, Student studentDetails) {
        // 1. Find the existing student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // 2. Update the fields
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setStudentRollNumber(studentDetails.getStudentRollNumber());

        // 3. Save to PostgreSQL
        Student updatedStudent = studentRepository.save(student);

        // 4. Send Audit Log to Kafka
        String auditMessage = String.format("ACTION: UPDATE | PERFORMED BY: %s | TARGET ID: %d | NEW NAME: %s", 
                                            getCurrentUser(), id, updatedStudent.getName());
        kafkaTemplate.send(AUDIT_TOPIC, auditMessage);

        return updatedStudent;
    }

    public void deleteStudent(Long id) {
        String studentName = studentRepository.findById(id)
                .map(Student::getName)
                .orElse("Unknown");

        studentRepository.deleteById(id);

        String auditMessage = String.format("ACTION: DELETE | PERFORMED BY: %s | TARGET ID: %d | NAME: %s", 
                                            getCurrentUser(), id, studentName);
        kafkaTemplate.send(AUDIT_TOPIC, auditMessage);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}