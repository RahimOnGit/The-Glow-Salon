package com.example.hairsalon.service;

import com.example.hairsalon.entity.Location;
import com.example.hairsalon.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new RuntimeException("Location not found"));
    }
}