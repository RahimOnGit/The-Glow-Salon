package com.example.hairsalon.service;

import com.example.hairsalon.entity.Service;
import com.example.hairsalon.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public Optional<Service> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return serviceRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return serviceRepository.existsByNameIgnoreCase(name);
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        return serviceRepository.existsByNameIgnoreCaseAndServiceIdNot(name, id);
    }

    public Service saveService(Service service) {
        // Trim whitespace from name
        if (service.getName() != null) {
            service.setName(service.getName().trim());
        }
        return serviceRepository.save(service);
    }

    public Service updateService(Long id, Service serviceDetails) {
        Optional<Service> optionalService = serviceRepository.findById(id);
        if (optionalService.isPresent()) {
            Service service = optionalService.get();
            service.setName(serviceDetails.getName().trim());
            service.setDuration(serviceDetails.getDuration());
            service.setPrice(serviceDetails.getPrice());
            return serviceRepository.save(service);
        } else {
            throw new RuntimeException("Service not found with ID: " + id);
        }
    }

    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new RuntimeException("Service not found with ID: " + id);
        }
        serviceRepository.deleteById(id);
    }
}