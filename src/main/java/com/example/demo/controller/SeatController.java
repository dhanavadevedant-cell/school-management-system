package com.example.demo.controller;

import com.example.demo.entity.Seat;
import com.example.demo.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "*")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Seat>> getAllSeats() {
        return ResponseEntity.ok(seatService.getAllSeats());
    }

    @PostMapping("/book/{seatNumber}")
    public ResponseEntity<?> bookSeat(@PathVariable String seatNumber, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username"); 

        try {
            Seat updatedSeat = seatService.bookSeat(seatNumber, username);
            return ResponseEntity.ok(updatedSeat);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage()); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Added: Compatible unbook route matching your path-variable pattern
    @PostMapping("/unbook/{seatNumber}")
    public ResponseEntity<?> unbookSeat(@PathVariable String seatNumber) {
        try {
            Seat updatedSeat = seatService.unbookSeat(seatNumber);
            return ResponseEntity.ok(updatedSeat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}