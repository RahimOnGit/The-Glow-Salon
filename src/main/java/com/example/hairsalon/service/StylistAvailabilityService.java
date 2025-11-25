package com.example.hairsalon.service;


import com.example.hairsalon.dto.BlockDateRequest;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.StylistAvailability;
import com.example.hairsalon.repository.StylistAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StylistAvailabilityService {

    private final StylistAvailabilityRepository repo;
    private final EmployeeService employeeService;

    public List<StylistAvailability> getByEmployee(Employee employee) {
        return repo.findByEmployeeOrderByDateAsc(employee);
    }

    @Transactional
    public StylistAvailability blockDate(Employee employee, BlockDateRequest req) {

        if (repo.existsByEmployeeAndDate(employee, req.date())) {
            throw new RuntimeException("This date is already blocked");
        }

        StylistAvailability sa = new StylistAvailability();
        sa.setEmployee(employee);
        sa.setDate(req.date());
        sa.setReason(req.reason());

        if (req.startTime() == null || req.endTime() == null) {
            sa.setType(StylistAvailability.AvailabilityType.BLOCKED);
        } else {
            sa.setType(StylistAvailability.AvailabilityType.PARTIAL);
            sa.setStartTime(req.startTime());
            sa.setEndTime(req.endTime());
        }

        return repo.save(sa);
    }

    @Transactional
    public void unblockDate(Employee employee, Long id) {
        StylistAvailability sa = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Blocked date not found"));

        if (!sa.getEmployee().equals(employee)) {
            throw new RuntimeException("Not authorized");
        }
        repo.delete(sa);
    }
}