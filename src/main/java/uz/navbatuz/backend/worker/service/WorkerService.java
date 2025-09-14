package uz.navbatuz.backend.worker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import uz.navbatuz.backend.common.AppointmentStatus;
import uz.navbatuz.backend.common.WorkerCategoryValidator;
import uz.navbatuz.backend.provider.model.BusinessHour;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.BusinessHourRepository;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.security.AuthorizationService;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.dto.*;
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
    private final BusinessHourRepository businessHourRepository;

    private static final Set<AppointmentStatus> BLOCKING_STATUSES = Set.of(
            AppointmentStatus.BOOKED,
            AppointmentStatus.RESCHEDULED,
            AppointmentStatus.COMPLETED   // include if completed should block TODAY's remaining time (usually not needed)
    );

    private static final Duration STEP = Duration.ofMinutes(10);


    @Transactional
    public Worker createWorker(CreateWorkerRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Provider provider = providerRepository.findById(request.provider())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!provider.getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to assign workers to this provider.");
        }

//        if (!WorkerCategoryValidator.isCompatible(provider.getCategory(), request.workerType())) {
//            throw new IllegalArgumentException("Worker type " + request.workerType() + " is not allowed in " + provider.getCategory() + " category");
//        }

        User user = userRepository.findById(request.user())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Worker worker = Worker.builder()
                //.id(UUID.randomUUID()) if only we want same worker to work in several shops
                .user(user)
                .provider(provider)
                .workerType(request.workerType())
                .status(Status.AVAILABLE)
                .hireDate(LocalDate.now())
                .avgRating(0.0f)
                .isActive(true)
                .build();

        return workerRepository.save(worker);
    }

    public ResponseEntity<WorkerDetailsDto> getWorker(UUID workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));

        WorkerDetailsDto dto = workerMapper.mapToDetails(worker);
        return ResponseEntity.ok(dto);
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

//        if (!worker.getProvider().getOwner().getId().equals(currentUserId)) {
//            throw new RuntimeException("You are not allowed to deactivate this worker.");
//        }

        worker.setActive(false);
        workerRepository.save(worker);
    }

    @Transactional
    public void activateWorker(UUID workerId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

//        if(!worker.getProvider().getOwner().getId().equals(currentUserId)) {
//            throw new RuntimeException("You are not allowed to activate this worker.");
//        }

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

    @Transactional
    public void setBreakDaily(UUID workerId, BreakRequest req) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        User currentUser = userRepository.getReferenceById(currentUserService.getCurrentUserId());
        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new RuntimeException("Unauthorized access to modify worker");
        }

        if (req == null || !req.isValid()) {
            throw new IllegalArgumentException("Invalid range: " + (req == null ? null : req.date()));
        }

        // optional: reject overlaps on the same day
        if (breakRepository.existsOverlap(workerId, req.date(), req.startTime(), req.endTime())) {
            throw new IllegalArgumentException("Break overlaps an existing break");
        }

        Break entity = Break.builder()
                .worker(worker)
                .date(req.date())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .build();

        breakRepository.save(entity);
    }

    @Transactional
    public void deleteBreak(UUID workerId, Long breakId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        User currentUser = userRepository.getReferenceById(currentUserService.getCurrentUserId());
        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new RuntimeException("Unauthorized access to modify worker");
        }

        int deleted = breakRepository.deleteByIdAndWorkerId(breakId, workerId);
        if (deleted == 0) {
            throw new RuntimeException("Break not found");
        }
    }

    public List<BreakResponse> getBreaks(UUID workerId, LocalDate from, LocalDate to) {
        return breakRepository.findByWorkerIdAndDateBetween(workerId, from, to);
    }

    @Transactional
    public void setActualAvailability(UUID workerId, ActualAvailabilityRequest req) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        User currentUser = userRepository.getReferenceById(currentUserService.getCurrentUserId());
        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new RuntimeException("Unauthorized access to modify worker");
        }
        if (req == null || !req.isValid()) {
            throw new IllegalArgumentException("Invalid range: " + (req == null ? null : req.date()));
        }

        // One per date: update if exists, else create
        Optional<ActualAvailability> existing =
                actualAvailabilityRepository.findByWorkerIdAndDate(workerId, req.date());

        ActualAvailability entity = existing.orElseGet(ActualAvailability::new);
        entity.setWorker(worker);
        entity.setDate(req.date());
        entity.setStartTime(req.startTime());
        entity.setEndTime(req.endTime());
        entity.setBufferBetweenAppointments(req.bufferBetweenAppointments());

        actualAvailabilityRepository.save(entity);
    }

    @Transactional
    public void deleteActualAvailability(UUID workerId, Long availabilityId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        User currentUser = userRepository.getReferenceById(currentUserService.getCurrentUserId());
        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new RuntimeException("Unauthorized access to modify worker");
        }

        int deleted = actualAvailabilityRepository.deleteByIdAndWorkerId(availabilityId, workerId);
        if (deleted == 0) throw new RuntimeException("Actual availability not found");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));

        // 1) Worker must be AVAILABLE
        if (worker.getStatus() == null || !worker.getStatus().name().equalsIgnoreCase("AVAILABLE")) {
            return List.of();
        }

        // 2) Lead-time cutoff (now + provider lead time, default 30m)
        int leadMin = Optional.ofNullable(worker.getProvider())
                .map(p -> p.getMinAdvanceBookingMinutes())
                .orElse(30);
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // If date is in the past → no slots
        if (date.isBefore(today)) return List.of();

        LocalTime minBookTimeToday = (date.isEqual(today))
                ? roundUp(nowTime.plusMinutes(Math.max(0, leadMin)), STEP)
                : LocalTime.MIN;

        // Provider working day bounds
        DayOfWeek day = date.getDayOfWeek();
        List<BusinessHour> businessHours = businessHourRepository.findByProviderId(worker.getProvider().getId());
        BusinessHour dayHours = businessHours.stream()
                .filter(b -> b.getDay() == day)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Provider closed on " + day
                ));

        LocalTime providerOpen = dayHours.getStartTime();
        LocalTime providerClose = dayHours.getEndTime();

        // Planned/Actual availability & buffer
        Optional<ActualAvailability> actualAvailability = actualAvailabilityRepository.findByWorkerIdAndDate(workerId, date);

        LocalTime start;
        LocalTime end;
        Duration buffer;

        if (actualAvailability.isPresent()) {
            ActualAvailability availability = actualAvailability.get();
            start  = availability.getStartTime();
            end    = availability.getEndTime();
            buffer = Optional.ofNullable(availability.getBufferBetweenAppointments()).orElse(Duration.ZERO);
        } else {
            PlannedAvailability planned = plannedAvailabilityRepository.findByWorkerIdAndDay(workerId, day);
            if (planned == null) {
                // on-leave / not planned → no slots
                return List.of();
            }
            start  = planned.getStartTime();
            end    = planned.getEndTime();
            buffer = Optional.ofNullable(planned.getBufferBetweenAppointments()).orElse(Duration.ZERO);
        }

        // Intersect with provider open hours
        if (start.isBefore(providerOpen)) start = providerOpen;
        if (end.isAfter(providerClose))  end   = providerClose;
        if (!start.isBefore(end)) return List.of();

        // Apply lead-time cutoff for TODAY
        if (date.isEqual(today) && minBookTimeToday.isAfter(start)) {
            start = minBookTimeToday;
            if (!start.isBefore(end)) return List.of();
        }

        // Subtract breaks
        List<Break> breaks = breakRepository.findByWorkerIdAndDate(workerId, date);
        List<TimeRange> available = new ArrayList<>();
        available.add(new TimeRange(start, end));
        for (Break b : breaks) {
            List<TimeRange> updated = new ArrayList<>();
            for (TimeRange r : available) {
                updated.addAll(r.subtract(b.getStartTime(), b.getEndTime()));
            }
            available = updated;
        }

        // Existing booked/held appointments
        List<Appointment> booked = appointmentRepository
                .findByWorkerIdAndDateAndStatusIn(workerId, date, BLOCKING_STATUSES);

        // Generate slots every 5 minutes, but ensure [slotStart, slotStart+serviceDuration] fits
        List<LocalTime> result = new ArrayList<>();
        for (TimeRange r : available) {
            // also apply the today's cutoff inside each fragment
            LocalTime rangeStart = (date.isEqual(today) && minBookTimeToday.isAfter(r.start()))
                    ? minBookTimeToday
                    : r.start();

            // start aligned to 5-minute grid
            LocalTime current = max(roundUp(rangeStart, STEP), r.start());

            while (!current.plus(serviceDuration).isAfter(r.end())) {
                LocalTime slotEnd = current.plus(serviceDuration);

                // Check collisions with existing appointments, considering buffer on both sides
                LocalTime finalCurrent = current;
                boolean clashes = booked.stream().anyMatch(app ->
                        overlapsWithBuffer(finalCurrent, slotEnd, app.getStartTime(), app.getEndTime(), buffer));

                if (!clashes) {
                    result.add(current);
                }

                current = current.plus(STEP); // 5-minute steps
            }
        }

        return result;
    }

    /* ---------------- helpers ---------------- */

    private static LocalTime roundUp(LocalTime t, Duration step) {
        long stepSec = step.getSeconds();
        long sec = t.toSecondOfDay();
        long rem = sec % stepSec;
        if (rem == 0) return t;
        long up = sec + (stepSec - rem);
        if (up >= 24 * 3600) up = 24 * 3600 - 1; // clamp to last second of day
        return LocalTime.ofSecondOfDay(up);
    }

    private static LocalTime max(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }

    /**
     * True if the candidate interval [candStart, candEnd] intersects
     * the existing interval [existStart - buffer, existEnd + buffer].
     */
    private static boolean overlapsWithBuffer(
            LocalTime candStart, LocalTime candEnd,
            LocalTime existStart, LocalTime existEnd,
            Duration buffer
    ) {
        LocalTime paddedStart = existStart.minus(buffer);
        if (paddedStart.isAfter(existStart)) paddedStart = LocalTime.MIDNIGHT; // underflow guard
        LocalTime paddedEnd = existEnd.plus(buffer);
        // Standard overlap check: a < d && c < b
        return candStart.isBefore(paddedEnd) && paddedStart.isBefore(candEnd);
    }

    private boolean overlaps(LocalTime slotStart, Duration serviceDuration, Appointment appointment) {
        LocalTime slotEnd = slotStart.plus(serviceDuration);
        return slotStart.isBefore(appointment.getEndTime()) &&
                slotEnd.isAfter(appointment.getStartTime());
    }

    public UUID requireProviderId(UUID workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"))
                .getProvider().getId();
    }

    @Transactional
    public WorkerDetailsDto updateWorker(UUID workerId, UpdateWorkerRequest req) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));

        UUID currentUserId = currentUserService.getCurrentUserId();
        User currentUser = userRepository.getReferenceById(currentUserId);
        if (!authorizationService.canModifyWorker(currentUser, worker)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        // Update User fields
        User u = worker.getUser();
        if (req.name() != null)       u.setName(req.name().trim());
        if (req.surname() != null)    u.setSurname(req.surname().trim());
        if (req.gender() != null)     u.setGender(req.gender());
        if (req.phoneNumber() != null)u.setPhoneNumber(req.phoneNumber().trim());
        if (req.email() != null)      u.setEmail(req.email().trim());
        if (req.avatarUrl() != null)  u.setAvatarUrl(req.avatarUrl().trim());

        // Update Worker fields
        if (req.workerType() != null) worker.setWorkerType(req.workerType());
        if (req.status() != null)     worker.setStatus(req.status());
        if (req.isActive() != null)   worker.setActive(req.isActive());

        // Persist
        userRepository.save(u);
        workerRepository.save(worker);

        // Return full details
        return workerMapper.mapToDetails(worker);
    }

    public ResponseEntity<WorkerDetailsDto> getCurrentWorker() {
        var uid = currentUserService.getCurrentUserId();
        var worker = workerRepository.findByUserId(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found for current user"));
        return ResponseEntity.ok(workerMapper.mapToDetails(worker));
    }

    public Worker getByIdOrThrow(UUID workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
    }
}
