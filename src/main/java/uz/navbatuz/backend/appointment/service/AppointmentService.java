package uz.navbatuz.backend.appointment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.appointment.dto.*;
import uz.navbatuz.backend.appointment.model.Appointment;
import uz.navbatuz.backend.appointment.model.AppointmentStatusHistory;
import uz.navbatuz.backend.appointment.repository.AppointmentRepository;
import uz.navbatuz.backend.appointment.repository.AppointmentStatusHistoryRepository;
import uz.navbatuz.backend.auth.service.AuthService;
import uz.navbatuz.backend.common.AppointmentStatus;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.guest.model.Guest;
import uz.navbatuz.backend.guest.repository.GuestRepository;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.providerclient.model.ProviderClient;
import uz.navbatuz.backend.providerclient.service.ProviderClientService;
import uz.navbatuz.backend.security.AuthorizationService;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;
import uz.navbatuz.backend.worker.service.WorkerService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final WorkerRepository workerRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final WorkerService workerService;
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final AppointmentStatusHistoryRepository historyRepository;
    private final AuthService authService;
    private final GuestRepository guestRepository;
    private final ProviderRepository providerRepository;
    private final ProviderClientService providerClientService;
    private final CurrentUserService currentUserService;

    private static final int RESCHEDULE_MIN_LEAD_MINUTES = 120;
    private static final int CANCEL_MIN_LEAD_MINUTES     = 120;

    private static final java.util.Set<AppointmentStatus> BLOCKING_STATUSES =
            java.util.EnumSet.of(AppointmentStatus.BOOKED, AppointmentStatus.RESCHEDULED);


    private User getCurrentUser() {
        UUID id = currentUserService.getCurrentUserId();
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean hasPermissionToModify(Appointment appointment, User currentUser) {
        return switch (currentUser.getRole()) {
            case CUSTOMER -> appointment.getCustomer() != null
                    && appointment.getCustomer().getUser() != null
                    && appointment.getCustomer().getUser().getId().equals(currentUser.getId());
            case WORKER -> appointment.getWorker() != null
                    && appointment.getWorker().getUser() != null
                    && appointment.getWorker().getUser().getId().equals(currentUser.getId());
            case OWNER -> appointment.getWorker() != null
                    && appointment.getWorker().getProvider() != null
                    && appointment.getWorker().getProvider().getOwner() != null
                    && appointment.getWorker().getProvider().getOwner().getId().equals(currentUser.getId());
            case ADMIN -> true;
            default -> false;
        };
    }

    private void assertLead(LocalDate d, LocalTime t, int minMinutes, String action) {
        var now  = LocalDateTime.now();
        var when = LocalDateTime.of(d, t);
        long diffMin = Duration.between(now, when).toMinutes();
        if (diffMin < minMinutes) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Too late to " + action + " (" + minMinutes + " min window)"
            );
        }
    }


    @Transactional
    public AppointmentResponse reschedule(UUID appointmentId, RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        // MVP rule: block reschedules inside fixed window
        assertLead(request.newDate(), request.newStartTime(), RESCHEDULE_MIN_LEAD_MINUTES, "reschedule");

        var oldStatus = appointment.getStatus();

        User currentUser = getCurrentUser();
        if (!hasPermissionToModify(appointment, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to modify this appointment");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only booked appointments can be rescheduled");
        }

        Worker worker = appointment.getWorker();
        ServiceEntity service = appointment.getService();

        // validate new slot
        var freeSlots = workerService.getFreeSlots(worker.getId(), request.newDate(), service.getDuration());
        if (!new java.util.HashSet<>(freeSlots).contains(request.newStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested time slot is not available");
        }

        // move and keep status BOOKED; log RESCHEDULED in history
        appointment.setDate(request.newDate());
        appointment.setStartTime(request.newStartTime());
        appointment.setEndTime(request.newStartTime().plus(service.getDuration()));
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointmentRepository.save(appointment);
        logStatusChange(appointment, oldStatus, AppointmentStatus.RESCHEDULED);

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse book(AppointmentNormalized cmd, UUID createdByUser) {
        var worker  = workerRepository.findById(cmd.workerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
        var service = serviceRepository.findById(cmd.serviceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        // block if already taken (only blocking statuses)
        if (appointmentRepository.existsByWorkerIdAndDateAndStartTimeAndStatusIn(
                cmd.workerId(), cmd.date(), cmd.startTime(), BLOCKING_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already booked");
        }

        // sanity: ensure requested time is part of free slots
        var free = workerService.getFreeSlots(cmd.workerId(), cmd.date(), service.getDuration());
        if (!new java.util.HashSet<>(free).contains(cmd.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested time slot is not available");
        }

        // resolve party
        Customer customer = null;
        uz.navbatuz.backend.guest.model.Guest guest = null;
        if (cmd.customerId() != null) {
            customer = customerRepository.findById(cmd.customerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        } else {
            guest = guestRepository.findById(cmd.guestId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found"));
            if (!guest.getProvider().getId().equals(worker.getProvider().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Guest belongs to another provider");
            }
        }

        var appt = Appointment.builder()
                .worker(worker)
                .service(service)
                .customer(customer)      // null if guest
                .guest(guest)            // null if customer
                .date(cmd.date())
                .startTime(cmd.startTime())
                .endTime(cmd.startTime().plus(service.getDuration()))
                .status(AppointmentStatus.BOOKED)
                .bookedDate(LocalDateTime.now())
                .createdByUser(createdByUser)
                .build();

        appointmentRepository.save(appt);

        // update provider_client index
        providerClientService.upsertFromAppointment(
                worker.getProvider(), customer, guest, null, null, createdByUser
        );

        return toResponse(appt);
    }

//    public List<AppointmentResponse> getWorkerAppointments(UUID workerId, LocalDate date, AppointmentStatus status) {
//        return appointmentRepository.findByWorkerIdAndDateAndStatusIn(workerId, date, status)
//                .stream().map(this::toResponse).toList();
//    }

//    public List<AppointmentResponse> getUpcomingWorkerAppointments(UUID workerId, LocalDate date) {
//        return appointmentRepository.findByWorkerIdAndDateAfter(workerId, date)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }

    public AppointmentResponse getAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return toResponse(appointment);
    }

    public AppointmentDetails getAppointmentDetails(UUID appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        return new AppointmentDetails(
                a.getId(),
                a.getDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getWorker().getProvider().getName(),
                a.getWorker().getProvider().getLocation().getAddressLine1(),
                a.getWorker().getProvider().getLocation().getCity(),
                a.getWorker().getProvider().getLocation().getCountryIso2(),
                a.getService().getName(),
                a.getService().getPrice(),
                a.getWorker().getUser().getName()
        );
    }

    public List<AppointmentResponse> getCustomerAppointments(UUID customerId) {
        return appointmentRepository.findByCustomerId(customerId)
                .stream().map(this::toResponse).toList();
    }

    public List<AppointmentSummaryResponse> getCustomerAppointments1(UUID customerId) {
        return appointmentRepository.findByCustomerId(customerId)
                .stream()
                .map(a -> new AppointmentSummaryResponse(
                        a.getId(),
                        a.getDate(),
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getStatus(),
                        a.getWorker().getUser().getName(),
                        a.getWorker().getProvider().getName(),
                        a.getService().getName()
                ))
                .toList();
    }

//    public List<AppointmentResponse> getMyAppointments(UUID userId) {
//        return appointmentRepository.findAllByCustomerOrWorker(userId)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }

    @Transactional
    public void cancelAppointment(UUID appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        User currentUser = getCurrentUser();
        if (!hasPermissionToModify(a, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to cancel");
        }

        // Block last-minute cancels for customers (MVP rule).
        // If you want to block staff too, remove this role check.
        if (currentUser.getRole() == uz.navbatuz.backend.common.Role.CUSTOMER) {
            assertLead(a.getDate(), a.getStartTime(), CANCEL_MIN_LEAD_MINUTES, "cancel");
        }

        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completed appointment cannot be cancelled");
        }

        var oldStatus = a.getStatus();
        a.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(a);
        logStatusChange(a, oldStatus, AppointmentStatus.CANCELLED);
    }


    @Transactional
    public void completeAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        AppointmentStatus oldStatus = appointment.getStatus();

        User currentUser = getCurrentUser();
        if (!hasPermissionToModify(appointment, currentUser)) {
            throw new RuntimeException("Not allowed to complete this appointment");
        }

        if(appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new RuntimeException("Only booked appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        logStatusChange(appointment, oldStatus, AppointmentStatus.COMPLETED);
    }

    private AppointmentResponse toResponse(Appointment a) {
        UUID customerId = (a.getCustomer() != null) ? a.getCustomer().getId() : null;
        UUID guestId = (a.getGuest() != null) ? a.getGuest().getId() : null;

        String guestMask = null;
        if (a.getGuest() != null && a.getGuest().getPhoneNumber() != null) {
            String p = a.getGuest().getPhoneNumber();
            guestMask = (p.length() < 4) ? "***" : "********" + p.substring(p.length() - 4);
        }

        return new AppointmentResponse(
                a.getId(),
                a.getWorker().getId(),
                a.getService().getId(),
                a.getWorker().getProvider().getId(),
                a.getDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                customerId,
                guestId,
                guestMask
        );
    }

    public void logStatusChange(Appointment appointment, AppointmentStatus oldStatus, AppointmentStatus newStatus) {
        var user = getCurrentUser(); // from SecurityContext
        var history = AppointmentStatusHistory.builder()
                .appointment(appointment)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .changedBy(user)
                .build();
        historyRepository.save(history);
    }

}

