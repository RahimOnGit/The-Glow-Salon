package com.example.hairsalon.dto;

import lombok.Data;

@Data
public class RatingRequest {
    private Long appointmentId;
    private Integer rating; // 1-5
    private String comment;
}