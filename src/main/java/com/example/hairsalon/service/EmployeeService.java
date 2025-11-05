package com.example.hairsalon.service;

import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public List<Employee> getAvailableEmployeesOnDate(LocalDate date, long maxBookingsPerDay) {
        return employeeRepository.findAvailableOnDate(date, maxBookingsPerDay);
    }
}