package uz.navbatuz.backend.appointment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.appointment.dto.AppointmentRequest;
import uz.navbatuz.backend.appointment.dto.AppointmentResponse;
import uz.navbatuz.backend.appointment.dto.RescheduleRequest;
import uz.navbatuz.backend.appointment.model.Appointment;
import uz.navbatuz.backend.appointment.model.AppointmentStatusHistory;
import uz.navbatuz.backend.appointment.repository.AppointmentRepository;
import uz.navbatuz.backend.appointment.repository.AppointmentStatusHistoryRepository;
import uz.navbatuz.backend.auth.service.AuthService;
import uz.navbatuz.backend.common.AppointmentStatus;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.security.AuthorizationService;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;
import uz.navbatuz.backend.worker.service.WorkerService;

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

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean hasPermissionToModify(Appointment appointment, User currentUser) {
        return switch (currentUser.getRole()) {
            case CUSTOMER -> appointment.getCustomer().getId().equals(currentUser.getId());
            case WORKER -> appointment.getWorker().getId().equals(currentUser.getId());
            case OWNER -> appointment.getWorker().getProvider().getOwner().getId().equals(currentUser.getId());
//            case RECEPTIONIST -> appointment.getWorker().getProvider().getReceptionists()
//                    .stream()
//                    .anyMatch(r -> r.getUser().getId().equals(currentUser.getId()));
            default -> false;
        };
    }

    @Transactional
    public AppointmentResponse reschedule(UUID appointmentId, RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        AppointmentStatus oldStatus = appointment.getStatus();

        User currentUser = getCurrentUser();
        if (!hasPermissionToModify(appointment, currentUser)) {
            throw new RuntimeException("You are not allowed to modify this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new RuntimeException("Only booked appointments can be rescheduled");
        }

        Worker worker = appointment.getWorker();
        ServiceEntity service = appointment.getService();

        var freeSlots = workerService.getFreeSlots(
                worker.getId(),
                request.newDate(),
                service.getDuration()
        );

        if (!freeSlots.contains(request.newStartTime())) {
            throw new RuntimeException("Requested time slot is not available");
        }

        appointment.setDate(request.newDate());
        appointment.setStartTime(request.newStartTime());
        appointment.setEndTime(request.newStartTime().plus(service.getDuration()));
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointmentRepository.save(appointment);
        logStatusChange(appointment, oldStatus, AppointmentStatus.RESCHEDULED);

        return new AppointmentResponse(
                appointment.getId(),
                worker.getId(),
                service.getId(),
                appointment.getCustomer().getId(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus()
        );
    }

    @Transactional
    public AppointmentResponse book(AppointmentRequest request) {
        Worker worker = workerRepository.findById(request.workerId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        ServiceEntity service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        boolean alreadyBooked = appointmentRepository
                .existsByWorkerIdAndDateAndStartTime(
                        request.workerId(),
                        request.date(),
                        request.startTime()
                );

        if (alreadyBooked) {
            throw new RuntimeException("Slot already booked by someone else");
        }

        var freeSlots = workerService.getFreeSlots(
                request.workerId(),
                request.date(),
                service.getDuration()
        );

        Set<LocalTime> slotTimes = new HashSet<>(freeSlots);

        if(!slotTimes.contains(request.startTime())) {
            throw new RuntimeException("Requested time slot is not available");
        }

        Appointment appointment = Appointment.builder()
                .worker(worker)
                .service(service)
                .customer(customer)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.startTime().plus(service.getDuration()))
                .status(AppointmentStatus.BOOKED)
                .bookedDate(LocalDateTime.now())
                .build();

        appointmentRepository.save(appointment);

        return new AppointmentResponse(
                appointment.getId(),
                worker.getId(),
                service.getId(),
                customer.getId(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus()
        );
    }

    public List<AppointmentResponse> getWorkerAppointments(UUID workerId, LocalDate date) {
        return appointmentRepository.findByWorkerIdAndDate(workerId, date)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

//    public List<AppointmentResponse> getUpcomingWorkerAppointments(UUID workerId, LocalDate date) {
//        return appointmentRepository.findByWorkerIdAndDateAfter(workerId, date)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }

    public AppointmentResponse getAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return mapToResponse(appointment);
    }

    public List<AppointmentResponse> getCustomerAppointments(UUID customerId) {
        return appointmentRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void cancelAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        AppointmentStatus oldStatus = appointment.getStatus();

        User currentUser = getCurrentUser();

        if (!hasPermissionToModify(appointment, currentUser)) {
            throw new RuntimeException("Not allowed to cancel");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Completed appointment cannot be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        logStatusChange(appointment, oldStatus, AppointmentStatus.CANCELLED);
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

    private AppointmentResponse mapToResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getWorker().getId(),
                a.getService().getId(),
                a.getCustomer().getId(),
                a.getDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus()
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

