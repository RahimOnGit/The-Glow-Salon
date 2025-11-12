package com.example.hairsalon.service;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.AppointmentStatus;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.Location;
import com.example.hairsalon.repository.AppointmentRepository;
import com.example.hairsalon.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Optional<Employee> getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserUserId(userId);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
    }


    public List<Employee> getEmployeesByLocation(Location location) {
        return employeeRepository.findByLocation(location);
    }

    public List<Employee> getAvailableEmployeesOnDate(LocalDate date, long maxBookingsPerDay) {
        return employeeRepository.findAvailableOnDate(date, maxBookingsPerDay);
    }

    public List<Employee> getAvailableEmployees(Location location, LocalDate date, LocalTime startTime, int duration) {
        List<Employee> employees = getEmployeesByLocation(location);
        return employees.stream()
                .filter(e -> !hasTimeConflict(e, date, startTime, duration))
                .collect(Collectors.toList());
    }

    private boolean hasTimeConflict(Employee employee, LocalDate date, LocalTime startTime, int duration) {
        List<Appointment> existing = appointmentRepository.findByEmployeeAndDate(employee, date);
        LocalTime endTime = startTime.plusMinutes(duration);

        for (Appointment a : existing) {
            if (!AppointmentStatus.PENDING.equals(a.getStatus())) continue;
            LocalTime aEndTime = a.getTime().plusMinutes(a.getTotalDuration());
            if (startTime.isBefore(aEndTime) && endTime.isAfter(a.getTime())) {
                return true;
            }
        }
        return false;
    }
}
