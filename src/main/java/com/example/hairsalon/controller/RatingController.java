package com.example.hairsalon.controller;

import com.example.hairsalon.dto.RatingRequest;
import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.AppointmentStatus;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.Rating;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.EmployeeService;
import com.example.hairsalon.service.RatingService;
import com.example.hairsalon.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final AppointmentService appointmentService;
    private final UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<?> submitRating(@RequestBody RatingRequest request, Authentication auth) {
        Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        String email = auth.getName();
        if (!appointment.getUser().getEmail().equals(email) || appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not authorized to rate this appointment"));
        }

        if (ratingService.getRatingByAppointmentId(request.getAppointmentId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "This appointment has already been rated"));
        }

        Rating rating = new Rating();
        rating.setAppointment(appointment);
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        ratingService.createRating(rating);

        return ResponseEntity.ok(Map.of("message", "Rating submitted successfully"));
    }

    @GetMapping("/employee/{employeeId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        Double avg = ratingService.getAverageRating(employee);
        return ResponseEntity.ok(avg != null ? avg : 0.0);
    }

    @GetMapping("/appointment/{appointmentId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkIfRated(@PathVariable Long appointmentId) {
        Optional<Rating> existingRating = ratingService.getRatingByAppointmentId(appointmentId);
        return ResponseEntity.ok(Map.of("rated", existingRating.isPresent()));
    }
}