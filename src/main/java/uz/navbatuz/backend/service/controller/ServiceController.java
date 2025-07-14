package uz.navbatuz.backend.service.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.dto.ServiceSummaryResponse;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.service.service.ServiceService;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.model.Worker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final ServiceRepository serviceRepository;

//    {
//        "name": "SPA - Men",
//            "description": "Professional SPA for men with wash",
//            "category": "SPA",
//            "price": 35.00,
//            "duration": 30,
//            "providerId": "8af65e6f-1d6a-4027-ba94-490fddf922b1",
//            "workerIds": [
//        "50017837-7163-452d-87a8-fc8ed8b88d46",
//                "bf7369f7-fca8-48e9-bbea-fad9e0cbde20"
//  ]
//    }


    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@RequestBody CreateServiceRequest request) {
        return ResponseEntity.ok(serviceService.createService(request));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceSummaryResponse>> getAllPublicServicesByProvider(@PathVariable UUID providerId) {
        List<ServiceSummaryResponse> services = serviceService.getAllPublicServicesByProvider(providerId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<ServiceSummaryResponse>> getAllActiveServicesByWorker(@PathVariable UUID workerId) {
        List<ServiceSummaryResponse> services = serviceService.getAllPublicServicesByWorker(workerId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceService.getService(serviceId));
    }

    @GetMapping("/provider/all/{providerId}")
    public ResponseEntity<List<ServiceResponse>> getAllServicesByProvider(@PathVariable UUID providerId) {
        List<ServiceResponse> services = serviceService.getAllServicesByProvider(providerId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/worker/all/{workerId}")
    public ResponseEntity<List<ServiceResponse>> getAllServicesByWorker(@PathVariable UUID workerId) {
        List<ServiceResponse> services = serviceService.getAllServicesByWorker(workerId);
        return ResponseEntity.ok(services);
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<Void> updateServiceById(@PathVariable UUID serviceId, @Valid @RequestBody ServiceResponse request) {
        serviceService.updateServiceById(serviceId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/deactivate/{serviceId}")
    public ResponseEntity<Void> deleteServiceById(@PathVariable UUID serviceId) {
        serviceService.deactivateById(serviceId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/activate/{serviceId}")
    public ResponseEntity<Void> activateServiceById(@PathVariable UUID serviceId) {
        serviceService.activateById(serviceId);
        return ResponseEntity.ok().build();
    }

//    @PatchMapping("/{serviceId}/add-worker")
//    public ResponseEntity<Void> addWorker(@PathVariable UUID serviceId, @RequestBody Worker worker) {
//        serviceService.addWorkertoService(serviceId, worker);
//        return ResponseEntity.ok().build();
//    }

//    @GetMapping("/{serviceId}/workers")
//    public ResponseEntity<List<WorkerResponse>> getAllWorkers(@PathVariable UUID serviceId) {
//        List<Worker> workers = serviceService.serviceProvidedByWorker(serviceId);
//    }

    @PutMapping("/{serviceId}/remove-worker/{workerId}")
    public ResponseEntity<Void> removeWorkerFromService(@PathVariable UUID serviceId, @PathVariable UUID workerId) {
        serviceService.removeWorkerFromService(serviceId, workerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{serviceId}/add-worker/{workerId}")
    public ResponseEntity<Void> addWorkerToService(@PathVariable UUID serviceId, @PathVariable UUID workerId) {
        serviceService.addWorkerToService(serviceId, workerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{serviceId}/workers")
    public ResponseEntity<List<WorkerResponseForService>> getWorkersByService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceService.getWorkersByServiceId(serviceId));
    }


    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID serviceId) {
        serviceService.deleteServiceById(serviceId);
        return ResponseEntity.ok().build();
    }

    // localhost:8080/search?category=BARBERSHOP&page=0&size=10

//    @GetMapping("/search")
//    public ResponseEntity<Page<ServiceResponse>> searchServices(
//            @RequestParam(required = false) String category,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//        return ResponseEntity.ok(serviceService.searchServices(category, pageable));
//    }

    // localhost:8080/api/services/search?category=BARBERSHOP&minPrice=10&maxPrice=100&page=0&size=10

    @GetMapping("/search")
    public ResponseEntity<Page<ServiceSummaryResponse>> searchServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceSummaryResponse> services = serviceService.searchServices(category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/public/worker/{workerId}")
    public ResponseEntity<List<ServiceSummaryResponse>> getVisibleServicesByWorker(@PathVariable UUID workerId) {
        return ResponseEntity.ok(serviceService.getPublicServicesByWorker(workerId));
    }


    // also by service id we should be able to see workers offering this and manage it if needed
}
