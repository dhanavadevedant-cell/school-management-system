package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", unique = true, nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private String status; // "AVAILABLE" or "BOOKED"

    @Column(name = "booked_by_user")
    private String bookedByUser; // Stores the username/email from Keycloak token

    // Default constructor required by Hibernate
    public Seat() {}

    public Seat(String seatNumber, String status, String bookedByUser) {
        this.seatNumber = seatNumber;
        this.status = status;
        this.bookedByUser = bookedByUser;
    }

    // --- EXPLICIT GETTERS & SETTERS (Prevents Compilation Failures) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookedByUser() {
        return bookedByUser;
    }

    public void setBookedByUser(String bookedByUser) {
        this.bookedByUser = bookedByUser;
    }
}