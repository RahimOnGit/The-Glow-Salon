import com.example.hairsalon.dto.AppointmentAdminDTO;
import com.example.hairsalon.entity.AppointmentStatus;
import com.example.hairsalon.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AppointmentAdminController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentAdminDTO>> getAllAppointments(
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String stylist,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<AppointmentAdminDTO> appointments =
                appointmentService.getAppointmentsForAdmin(customer, stylist, status, from, to);

        return appointments.isEmpty()
                ? ResponseEntity.ok(Collections.emptyList())
                : ResponseEntity.ok(appointments);
    }
}
