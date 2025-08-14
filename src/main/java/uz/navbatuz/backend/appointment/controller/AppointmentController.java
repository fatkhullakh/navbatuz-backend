package uz.navbatuz.backend.appointment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.appointment.dto.AppointmentRequest;
import uz.navbatuz.backend.appointment.dto.AppointmentResponse;
import uz.navbatuz.backend.appointment.dto.AppointmentSummaryResponse;
import uz.navbatuz.backend.appointment.dto.RescheduleRequest;
import uz.navbatuz.backend.appointment.service.AppointmentService;
import uz.navbatuz.backend.user.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> book(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.book(request));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<AppointmentResponse>> getWorkerAppointments(
            @PathVariable UUID workerId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(appointmentService.getWorkerAppointments(workerId, date));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AppointmentResponse>> getCustomerAppointments(@PathVariable UUID customerId) {
        return ResponseEntity.ok(appointmentService.getCustomerAppointments(customerId));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointment(appointmentId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN', 'CUSTOMER')")
    @PutMapping("/{appointmentId}/cancel")
    public void cancelAppointment(@PathVariable UUID appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/{appointmentId}/complete")
    public ResponseEntity<Void> completeAppointment(@PathVariable UUID appointmentId) {
        appointmentService.completeAppointment(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN', 'CUSTOMER')")
    @PutMapping("/{appointmentId}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(@PathVariable UUID appointmentId, @RequestBody RescheduleRequest request) {
        return ResponseEntity.ok(
                appointmentService.reschedule(appointmentId, request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @GetMapping("/me")
    public ResponseEntity<List<AppointmentSummaryResponse>> myAppointments(Authentication auth) {
        String email = auth.getName();                   // comes from JWT principal
        UUID customerId = userService.findIdByEmail(email); // implement below
        return ResponseEntity.ok(appointmentService.getCustomerAppointments1(customerId));
    }

    //TODO: Book again feature
}
