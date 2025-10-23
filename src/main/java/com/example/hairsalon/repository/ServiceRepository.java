package com.example.hairsalon.repository;

import com.example.hairsalon.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // Check if a service exists by name (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Check if a service exists by name excluding a specific ID (for updates)
    boolean existsByNameIgnoreCaseAndServiceIdNot(String name, Long serviceId);
}