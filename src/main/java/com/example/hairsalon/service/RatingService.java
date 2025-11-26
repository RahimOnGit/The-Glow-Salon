package com.example.hairsalon.service;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.Rating;
import com.example.hairsalon.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;

    public Optional<Rating> getRatingByAppointmentId(Long appointmentId) {
        return ratingRepository.findByAppointmentAppointmentId(appointmentId);
    }

    @Transactional
    public Rating createRating(Rating rating) {
        return ratingRepository.save(rating);
    }

    public Double getAverageRating(Employee employee) {
        List<Rating> ratings = ratingRepository.findByAppointmentEmployee(employee);
        if (ratings.isEmpty()) {
            return null;
        }
        return ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }
}