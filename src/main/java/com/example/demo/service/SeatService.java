package com.example.demo.service;

import com.example.demo.entity.Seat;
import com.example.demo.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    @Transactional
    public Seat bookSeat(String seatNumber, String username) {
        // 1. Lock the row immediately upon pulling it
        Seat seat = seatRepository.findBySeatNumberWithLock(seatNumber)
                .orElseThrow(() -> new RuntimeException("Seat " + seatNumber + " does not exist."));

        // 2. Check if already booked while this thread was acquiring the lock
        if ("BOOKED".equals(seat.getStatus())) {
            throw new IllegalStateException("Sorry, this seat has already been booked by another user!");
        }

        // 3. Update seat details
        seat.setStatus("BOOKED");
        seat.setBookedByUser(username);

        return seatRepository.save(seat);
    }

    /**
     * Releases a seat reservation and makes it open for allocation again.
     */
    @Transactional
    public Seat unbookSeat(String seatNumber) {
        // 1. Fetch the seat from the database (optionally use lock if multiple admins can toggle simultaneously)
        Seat seat = seatRepository.findBySeatNumberWithLock(seatNumber)
                .orElseThrow(() -> new RuntimeException("Seat " + seatNumber + " does not exist."));

        // 2. Reset the properties back to default
        seat.setStatus("AVAILABLE");
        seat.setBookedByUser(null);

        // 3. Save and return the updated entity
        return seatRepository.save(seat);
    }
}