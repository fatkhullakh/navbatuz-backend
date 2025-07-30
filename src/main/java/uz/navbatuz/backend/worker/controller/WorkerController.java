package uz.navbatuz.backend.worker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.availability.dto.*;
import uz.navbatuz.backend.availability.model.Break;
import uz.navbatuz.backend.availability.model.PlannedAvailability;
import uz.navbatuz.backend.worker.dto.CreateWorkerRequest;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.mapper.WorkerMapper;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.service.WorkerService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {
    private final WorkerService workerService;
    private final WorkerMapper workerMapper;

//    @PostMapping
//    public ResponseEntity<Worker> createWorker(@RequestBody UUID userId, @RequestBody UUID providerId) {
//        Worker worker = workerService.createWorker(userId, providerId);
//        return new ResponseEntity<>(worker, HttpStatus.CREATED);
//    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<WorkerResponse> createWorker(@RequestBody CreateWorkerRequest request) {
        Worker worker = workerService.createWorker(request);
        WorkerResponse response = workerMapper.mapToResponse(worker);
        return ResponseEntity.ok(response);
    }

//    {
//            "user": "413eb378-e28f-4c66-8a4e-64dba33e30f7",
//            "provider": "8af65e6f-1d6a-4027-ba94-490fddf922b1",
//            "workerType": "BARBER"
//    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'ADMIN')")
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<WorkerResponse>> getAllWorkersOfProvider(@PathVariable UUID providerId) {
        List<WorkerResponse> workers = workerService.getAllWorkersOfProvider(providerId);
        return ResponseEntity.ok(workers);
    }

    @GetMapping("/public/provider/{providerId}")
    public ResponseEntity<List<WorkerResponseForService>> getAllActiveWorkersOfProvider(@PathVariable UUID providerId) {
        List<WorkerResponseForService> workers = workerService.getAllActiveWorkersOfProvider(providerId);
        return ResponseEntity.ok(workers);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/{workerId}/deactivate")
    public ResponseEntity<Void> deactivateWorker(@PathVariable UUID workerId) {
        workerService.deactivateWorker(workerId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/{workerId}/activate")
    public ResponseEntity<Void> activateWorker(@PathVariable UUID workerId) {
        workerService.activateWorker(workerId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PostMapping("/availability/planned/{workerId}")
    public ResponseEntity<Void> setPlannedAvailability(@PathVariable UUID workerId, @RequestBody List<PlannedAvailabilityRequest> request) {
        workerService.setPlannedAvailability(workerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/availability/planned/{workerId}")
    public ResponseEntity<List<PlannedAvailabilityResponse>> getPlannedAvailability(@PathVariable UUID workerId) {
        return ResponseEntity.ok(workerService.getPlannedAvailability(workerId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PostMapping("/availability/actual/{workerId}")
    public ResponseEntity<Void> setActualAvailability(@PathVariable UUID workerId, @RequestBody List<ActualAvailabilityRequest> request) {
        workerService.setActualAvailability(workerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/availability/actual/{workerId}")
    public ResponseEntity<List<ActualAvailabilityResponse>> getActualAvailability(@PathVariable UUID workerId,
                                                                                  @RequestParam LocalDate from,
                                                                                  @RequestParam LocalDate to) {
        return ResponseEntity.ok(workerService.getActualAvailability(workerId, from, to));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PostMapping("/availability/break/{workerId}")
    public ResponseEntity<Void> setBreaks(@PathVariable UUID workerId, @RequestBody List<BreakRequest> request) {
        workerService.setBreak(workerId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/availability/break/{workerId}")
    public ResponseEntity<List<BreakResponse>> getBreaks(@PathVariable UUID workerId,
                                                 @RequestParam LocalDate from,
                                                 @RequestParam LocalDate to) {
        return ResponseEntity.ok(workerService.getBreaks(workerId, from, to));
    }

    @GetMapping("/free-slots/{workerId}")
    public ResponseEntity<List<LocalTime>> getFreeSlots(
            @PathVariable UUID workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int serviceDurationMinutes) {
        Duration duration = Duration.ofMinutes(serviceDurationMinutes);
        return ResponseEntity.ok(workerService.getFreeSlots(workerId, date, duration));
    }
}
