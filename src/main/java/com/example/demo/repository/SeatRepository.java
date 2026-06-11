package com.example.demo.repository;

import com.example.demo.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // The lock ensures thread safety during concurrent booking requests
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.seatNumber = :seatNumber")
    Optional<Seat> findBySeatNumberWithLock(@Param("seatNumber") String seatNumber);

    Optional<Seat> findBySeatNumber(String seatNumber);
}