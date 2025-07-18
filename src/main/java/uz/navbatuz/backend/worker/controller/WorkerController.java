package uz.navbatuz.backend.worker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.worker.dto.CreateWorkerRequest;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
import uz.navbatuz.backend.worker.mapper.WorkerMapper;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.service.WorkerService;

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

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<WorkerResponse>> getWorkersOfProvider(@PathVariable UUID providerId) {
        List<WorkerResponse> workers = workerService.getAllWorkerOfProvider(providerId);
        return ResponseEntity.ok(workers);
    }


    @PutMapping("/{workerId}/deactivate")
    public ResponseEntity<Void> deactivateWorker(@PathVariable UUID workerId) {
        workerService.deactivateWorker(workerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{workerId}/activate")
    public ResponseEntity<Void> activateWorker(@PathVariable UUID workerId) {
        workerService.activateWorker(workerId);
        return ResponseEntity.ok().build();
    }

}
