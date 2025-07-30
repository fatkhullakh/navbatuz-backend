package uz.navbatuz.backend.worker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.appointment.model.Appointment;
import uz.navbatuz.backend.appointment.repository.AppointmentRepository;
import uz.navbatuz.backend.availability.dto.*;
import uz.navbatuz.backend.availability.model.ActualAvailability;
import uz.navbatuz.backend.availability.model.Break;
import uz.navbatuz.backend.availability.model.PlannedAvailability;
import uz.navbatuz.backend.availability.repository.ActualAvailabilityRepository;
import uz.navbatuz.backend.availability.repository.BreakRepository;
import uz.navbatuz.backend.availability.repository.PlannedAvailabilityRepository;
import uz.navbatuz.backend.common.WorkerCategoryValidator;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.security.AuthorizationService;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.dto.CreateWorkerRequest;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.mapper.WorkerMapper;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;
import uz.navbatuz.backend.common.Status;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService {
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final CurrentUserService currentUserService;
    private final WorkerMapper workerMapper;
    private final PlannedAvailabilityRepository plannedAvailabilityRepository;
    private final ActualAvailabilityRepository actualAvailabilityRepository;
    private final BreakRepository breakRepository;
    private final AuthorizationService authorizationService;
    private final AppointmentRepository appointmentRepository;


    @Transactional
    public Worker createWorker(CreateWorkerRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Provider provider = providerRepository.findById(request.provider())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!provider.getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to assign workers to this provider.");
        }

        if (!WorkerCategoryValidator.isCompatible(provider.getCategory(), request.workerType())) {
            throw new IllegalArgumentException("Worker type " + request.workerType() + " is not allowed in " + provider.getCategory() + " category");
        }

        User user = userRepository.findById(request.user())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Worker worker = Worker.builder()
                //.id(UUID.randomUUID()) if only we want same worker to work in several shops
                .user(user)
                .provider(provider)
                .workerType(request.workerType())
                .status(Status.AVAILABLE)
                .hireDate(LocalDate.now())
                .avgRating(3.0f)
                .isActive(true)
                .build();

        return workerRepository.save(worker);
    }


    public List<WorkerResponseForService> getAllActiveWorkersOfProvider(UUID providerId) {
        return workerRepository.findByProviderIdAndIsActiveTrue(providerId)
                .stream()
                .map(workerMapper::mapToWorkerResponse)
                .toList();
    }

    public List<WorkerResponse> getAllWorkersOfProvider(UUID providerId) {
        return workerRepository.findByProviderId(providerId)
                .stream()
                .map(workerMapper::mapToResponse)
                .toList();
    }


//    public Worker createWorker(UUID userId, UUID providerId, WorkerType workerType) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        Provider provider = providerRepository.findById(providerId)
//                .orElseThrow(() -> new RuntimeException("Provider not found"));
//
//        Worker worker = Worker.builder()
//                .user(user)
//                .provider(provider)
//                .workerType(workerType)
//                .status(Status.AVAILABLE)
//                .hireDate(LocalDate.now())
//                .terminationDate(null)
//                .avgRating(3.0f)
//                .isActive(true)
//                .build();
//
//        return workerRepository.save(worker);
//    }
//
//    public List<Worker> getAllWorkerOfProvider(UUID providerId) {
//        return workerRepository.findByProviderIdAndIsActiveTrue(providerId);
//    }

    @Transactional
    public void deactivateWorker(UUID workerId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (!worker.getProvider().getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to deactivate this worker.");
        }

        worker.setActive(false);
        workerRepository.save(worker);
    }

    @Transactional
    public void activateWorker(UUID workerId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if(!worker.getProvider().getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to activate this worker.");
        }

        worker.setActive(true);
        workerRepository.save(worker);
    }


//    @Transactional
//    public void setPlannedAvailability(UUID workerId, List<PlannedAvailabilityRequest> requests) {
//        Worker worker = workerRepository.findById(workerId)
//                .orElseThrow(() -> new RuntimeException("Worker not found"));
//
//        plannedAvailabilityRepository.deleteByWorkerId(workerId);
//
//        Set<DayOfWeek> seen = new HashSet<>();
//        List<PlannedAvailability> entries = new ArrayList<>();
//
//        for (PlannedAvailabilityRequest req : requests) {
//            if (!req.isValid())
//                throw new IllegalArgumentException("Invalid range for " + req.day());
//
//            if (!seen.add(req.day()))
//                throw new IllegalArgumentException("Duplicate day: " + req.day());
//
//            entries.add(PlannedAvailability.builder()
//                    .worker(worker)
//                    .day(req.day())
//                    .startTime(req.startTime())
//                    .endTime(req.endTime())
//                    .bufferBetweenAppointments(req.bufferBetweenAppointments())
//                    .build());
//        }
//
//        plannedAvailabilityRepository.saveAll(entries);
//    }

    @Transactional
    public void setBreak(UUID workerId, List<BreakRequest> requests) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        User currentUser = userRepository.getReferenceById(currentUserService.getCurrentUserId());

        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new RuntimeException("Unauthorized access to modify worker");
        }

        breakRepository.deleteByWorkerIdAndDateIn(workerId, requests.stream().map(BreakRequest::date).toList());

        List<Break> breaks = new ArrayList<>();
        for (BreakRequest req : requests) {
            if(!req.isValid())
                throw new IllegalArgumentException("Invalid range: " + req.date());

            breaks.add(Break.builder()
                    .worker(worker)
                    .date(req.date())
                    .startTime(req.startTime())
                    .endTime(req.endTime())
                    .build());
        }

        breakRepository.saveAll(breaks);
    }

    public List<BreakResponse> getBreaks(UUID workerId, LocalDate from, LocalDate to) {
        return breakRepository.findByWorkerIdAndDateBetween(workerId, from, to);
    }

    @Transactional
    public void setActualAvailability(UUID workerId, List<ActualAvailabilityRequest> requests) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        User currentUser = userRepository.getReferenceById(currentUserService.getCurrentUserId());

        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new RuntimeException("Unauthorized access to modify worker");
        }

        actualAvailabilityRepository.deleteByWorkerIdAndDateIn(workerId, requests.stream().map(ActualAvailabilityRequest::date).toList());

        List<ActualAvailability> list = new ArrayList<>();
        for (ActualAvailabilityRequest req : requests) {
            if (!req.isValid())
                throw new IllegalArgumentException("Invalid range: " + req.date());

            list.add(ActualAvailability.builder()
                    .worker(worker)
                    .date(req.date())
                    .startTime(req.startTime())
                    .endTime(req.endTime())
                    .bufferBetweenAppointments(req.bufferBetweenAppointments())
                    .build());
        }
        actualAvailabilityRepository.saveAll(list);
    }

    public List<ActualAvailabilityResponse> getActualAvailability(UUID workerId, LocalDate from, LocalDate to) {
        return actualAvailabilityRepository.findByWorkerIdAndDateBetween(workerId, from, to);
    }


    @Transactional
    public void setPlannedAvailability(UUID workerId, List<PlannedAvailabilityRequest> requests) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        User currentUser = userRepository.getReferenceById(currentUserService.getCurrentUserId());

        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new RuntimeException("Unauthorized access to modify worker");
        }

        List<PlannedAvailability> existing = plannedAvailabilityRepository.findByWorkerId(workerId);

        Map<DayOfWeek, PlannedAvailability> existingByDay = existing.stream()
                .collect(Collectors.toMap(PlannedAvailability::getDay, Function.identity()));

        Set<DayOfWeek> incomingDays = new HashSet<>();
        List<PlannedAvailability> toSave = new ArrayList<>();

        for (PlannedAvailabilityRequest req : requests) {
            if (!req.isValid())
                throw new IllegalArgumentException("Invalid time range for: " + req.day());

            if (!incomingDays.add(req.day()))
                throw new IllegalArgumentException("Duplicate day in request: " + req.day());

            PlannedAvailability entry = existingByDay.getOrDefault(req.day(), new PlannedAvailability());

            entry.setWorker(worker);
            entry.setDay(req.day());
            entry.setStartTime(req.startTime());
            entry.setEndTime(req.endTime());
            entry.setBufferBetweenAppointments(req.bufferBetweenAppointments());

            toSave.add(entry);
        }

        plannedAvailabilityRepository.saveAll(toSave);

        List<PlannedAvailability> toDelete = existing.stream()
                .filter(pa -> !incomingDays.contains(pa.getDay()))
                .toList();

        plannedAvailabilityRepository.deleteAll(toDelete);
    }


    public List<PlannedAvailabilityResponse> getPlannedAvailability(UUID workerId) {
        return plannedAvailabilityRepository.getByWorkerId(workerId);
    }

    public List<LocalTime> getFreeSlots(UUID workerId, LocalDate date, Duration serviceDuration) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        Optional<ActualAvailability> actualAvailability = actualAvailabilityRepository
                .findByWorkerIdAndDate(workerId, date);

        LocalTime start;
        LocalTime end;
        Duration buffer;

        if (actualAvailability.isPresent()) {
            ActualAvailability availability = actualAvailability.get();
            start = availability.getStartTime();
            end = availability.getEndTime();
            buffer = availability.getBufferBetweenAppointments();
        } else {
            DayOfWeek day = date.getDayOfWeek();
            PlannedAvailability planned = plannedAvailabilityRepository.findByWorkerIdAndDay(workerId, day);

            if (planned == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No planned availability for worker " + workerId + " on " + day
                );
            }

            start = planned.getStartTime();
            end = planned.getEndTime();
            buffer = planned.getBufferBetweenAppointments();
        }

        List<Break> breaks = breakRepository.findByWorkerIdAndDate(workerId, date);

        List<TimeRange> availableTimeRanges = List.of(new TimeRange(start, end));

        for (Break b : breaks) {
            List<TimeRange> updated = new ArrayList<>();
            for (TimeRange range : availableTimeRanges) {
                updated.addAll(range.subtract(b.getStartTime(), b.getEndTime()));
            }
            availableTimeRanges = updated;
        }

        List<Appointment> bookedAppointments =
                appointmentRepository.findByWorkerIdAndDate(workerId, date);

        List<LocalTime> slots = new ArrayList<>();
        for (TimeRange range : availableTimeRanges) {
            LocalTime current = range.start();
            while (!current.plus(serviceDuration).isAfter(range.end())) {

                final LocalTime slotStart = current;

                boolean isFree = bookedAppointments.stream()
                        .noneMatch(app -> overlaps(slotStart, serviceDuration, app));

                if (isFree) {
                    slots.add(slotStart);
                }

                current = current.plus(serviceDuration).plus(buffer);
            }
        }

        return slots;
    }

    private boolean overlaps(LocalTime slotStart, Duration serviceDuration, Appointment appointment) {
        LocalTime slotEnd = slotStart.plus(serviceDuration);
        return slotStart.isBefore(appointment.getEndTime()) &&
                slotEnd.isAfter(appointment.getStartTime());
    }




}
