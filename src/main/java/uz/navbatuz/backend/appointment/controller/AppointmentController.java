package uz.navbatuz.backend.appointment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.appointment.dto.*;
import uz.navbatuz.backend.appointment.service.AppointmentService;
import uz.navbatuz.backend.customer.service.CustomerService;
import uz.navbatuz.backend.guest.service.GuestService;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.service.UserService;
import uz.navbatuz.backend.worker.service.WorkerService;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final CustomerService customerService;
    private final WorkerService workerService;
    private final GuestService guestService;
    private final CurrentUserService currentUserService;

    /** Allow customers AND staff. Customers book for self; staff always book as guest. */
    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','RECEPTIONIST','WORKER','ADMIN')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> book(@RequestBody AppointmentRequest req, Authentication auth) {

        boolean isCustomerRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        UUID effectiveCustomerId = null;
        UUID effectiveGuestId = null;

        if (isCustomerRole) {
            // Customer booking for self
            effectiveCustomerId = customerService.requireCustomerIdByUsername(auth.getName());
        } else {
            // Staff booking
            if (req.customerId() != null) {
                // explicitly chosen customer from UI
                effectiveCustomerId = req.customerId();
            } else if (req.guestId() != null) {
                // explicitly chosen guest
                effectiveGuestId = req.guestId();
            } else if (req.guestPhone() != null && !req.guestPhone().isBlank()) {
                // Staff typed a phone: if that phone belongs to a registered customer -> book as CUSTOMER
                var maybeCust = customerService.findCustomerIdByPhoneE164(req.guestPhone());
                if (maybeCust.isPresent()) {
                    effectiveCustomerId = maybeCust.get();
                } else {
                    // otherwise create/find a Guest for this provider
                    var providerId = workerService.requireProviderId(req.workerId());
                    UUID actorUserId = currentUserService.getCurrentUserId();
                    var guest = guestService.findOrCreate(
                            providerId,
                            req.guestPhone(),            // guestService should normalize
                            req.guestName(),
                            actorUserId,
                            auth.getAuthorities().iterator().next().getAuthority()
                    );
                    effectiveGuestId = guest.getId();
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide customerId OR guestId OR guestPhone");
            }
        }

        if ((effectiveCustomerId == null) == (effectiveGuestId == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exactly one of customer or guest must be set");
        }

        var normalized = new AppointmentNormalized(
                req.workerId(), req.serviceId(), req.date(), req.startTime(),
                effectiveCustomerId, effectiveGuestId
        );

        UUID actorUserId = currentUserService.getCurrentUserId();
        var resp = appointmentService.book(normalized, actorUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

//    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
//    @GetMapping("/worker/{workerId}")
//    public ResponseEntity<List<AppointmentResponse>> getWorkerAppointments(
//            @PathVariable UUID workerId,
//            @RequestParam LocalDate date
//    ) {
//        return ResponseEntity.ok(appointmentService.getWorkerAppointments(workerId, date));
//    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AppointmentResponse>> getCustomerAppointments(@PathVariable UUID customerId) {
        return ResponseEntity.ok(appointmentService.getCustomerAppointments(customerId));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDetails> getAppointmentDetails(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentDetails(appointmentId));
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

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
    @GetMapping("/worker/{workerId}/day")
    public ResponseEntity<List<AppointmentResponse>> getWorkerDay(
            @PathVariable UUID workerId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(appointmentService.getWorkerAppointmentsDay(workerId, date));
    }

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
    @GetMapping("/worker/{workerId}/day/staff")
    public ResponseEntity<List<AppointmentResponseStaff>> getWorkerDayStaff(
            @PathVariable UUID workerId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(appointmentService.getWorkerAppointmentsDayStaff(workerId, date));
    }

    // new staff details endpoint
    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
    @GetMapping("/{appointmentId}/staff")
    public ResponseEntity<AppointmentDetailsStaff> getAppointmentDetailsStaff(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentDetailsStaff(appointmentId));
    }

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
    @PutMapping("/{appointmentId}/no-show")
    public ResponseEntity<Void> markNoShow(@PathVariable UUID appointmentId) {
        appointmentService.markNoShow(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
    @PutMapping("/{appointmentId}/undo-no-show")
    public ResponseEntity<Void> undoNoShow(@PathVariable UUID appointmentId) {
        appointmentService.undoNoShow(appointmentId);
        return ResponseEntity.ok().build();
    }

    //TODO: Book again feature
}
