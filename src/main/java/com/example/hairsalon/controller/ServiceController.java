package com.example.hairsalon.controller;

import com.example.hairsalon.dto.ServiceRequest;
import com.example.hairsalon.entity.Service;
import com.example.hairsalon.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
@PreAuthorize("hasRole('ADMIN')")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<Service>> getAllServices() {
        List<Service> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @PostMapping
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceRequest serviceRequest) {
        try {
            // Check if service with same name already exists
            if (serviceService.existsByName(serviceRequest.getName())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Service already exists");
                errorResponse.put("message", "A service with this name already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Map DTO to entity
            Service service = new Service();
            service.setName(serviceRequest.getName());
            service.setDuration(serviceRequest.getDuration());
            service.setPrice(serviceRequest.getPrice());

            Service savedService = serviceService.saveService(service);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedService);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create service");
            errorResponse.put("message", "An error occurred while creating the service");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequest serviceRequest) {
        try {
            // Check if service exists
            if (!serviceService.existsById(id)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Service not found");
                errorResponse.put("message", "No service found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Check if another service has the same name
            if (serviceService.existsByNameAndIdNot(serviceRequest.getName(), id)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Service name already exists");
                errorResponse.put("message", "Another service with this name already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            Service serviceDetails = new Service();
            serviceDetails.setName(serviceRequest.getName());
            serviceDetails.setDuration(serviceRequest.getDuration());
            serviceDetails.setPrice(serviceRequest.getPrice());

            Service updatedService = serviceService.updateService(id, serviceDetails);
            return ResponseEntity.ok(updatedService);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update service");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            if (!serviceService.existsById(id)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Service not found");
                errorResponse.put("message", "No service found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            serviceService.deleteService(id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete service");
            errorResponse.put("message", "Cannot delete service. It may be associated with appointments.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errors.put("error", "Validation failed");
        errors.put("details", fieldErrors);
        return ResponseEntity.badRequest().body(errors);
    }
}