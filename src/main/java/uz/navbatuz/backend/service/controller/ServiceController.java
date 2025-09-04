package uz.navbatuz.backend.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.dto.ServiceSummaryResponse;
import uz.navbatuz.backend.service.mapper.ServiceMapper;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.service.service.ServiceService;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final ServiceRepository serviceRepository;
    private final CurrentUserService currentUserService;
    private final ServiceMapper serviceMapper;

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@RequestBody CreateServiceRequest request) {
        UUID actorId = currentUserService.getCurrentUserId();
        var created = serviceService.createService(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/public/provider/{providerId}/services")
    public ResponseEntity<List<ServiceSummaryResponse>> getAllPublicServicesByProvider(@PathVariable UUID providerId) {
        return ResponseEntity.ok(serviceService.getAllPublicServicesByProvider(providerId));
    }

    @GetMapping("/public/worker/{workerId}/services")
    public ResponseEntity<List<ServiceSummaryResponse>> getAllActiveServicesByWorker(@PathVariable UUID workerId) {
        return ResponseEntity.ok(serviceService.getAllPublicServicesByWorker(workerId));
    }

    @GetMapping("/public/{serviceId}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceService.getService(serviceId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'ADMIN', 'WORKER')")
    @GetMapping("/provider/all/{providerId}")
    public ResponseEntity<List<ServiceResponse>> getAllServicesByProvider(@PathVariable UUID providerId) {
        return ResponseEntity.ok(serviceService.getAllServicesByProvider(providerId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @GetMapping("/worker/all/{workerId}")
    public ResponseEntity<List<ServiceResponse>> getAllServicesByWorker(@PathVariable UUID workerId) {
        return ResponseEntity.ok(serviceService.getAllServicesByWorker(workerId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/{serviceId}")
    public ResponseEntity<Void> updateServiceById(@PathVariable UUID serviceId,
                                                  @Valid @RequestBody ServiceResponse request) {
        UUID actorId = currentUserService.getCurrentUserId();
        serviceService.updateServiceById(serviceId, request, actorId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/deactivate/{serviceId}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID serviceId) {
        serviceService.deactivateById(serviceId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/activate/{serviceId}")
    public ResponseEntity<Void> activate(@PathVariable UUID serviceId) {
        serviceService.activateById(serviceId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/{serviceId}/remove-worker/{workerId}")
    public ResponseEntity<Void> removeWorkerFromService(@PathVariable UUID serviceId, @PathVariable UUID workerId) {
        serviceService.removeWorkerFromService(serviceId, workerId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/{serviceId}/add-worker/{workerId}")
    public ResponseEntity<Void> addWorkerToService(@PathVariable UUID serviceId, @PathVariable UUID workerId) {
        serviceService.addWorkerToService(serviceId, workerId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'ADMIN')")
    @GetMapping("/{serviceId}/workers")
    public ResponseEntity<List<WorkerResponseForService>> getWorkersByService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceService.getWorkersByServiceId(serviceId));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> delete(@PathVariable UUID serviceId) {
        serviceService.deleteService(serviceId, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<ServiceSummaryResponse>> searchServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(serviceService.searchServices(category, minPrice, maxPrice, pageable));
    }

    record ImageUrlRequest(String url) {}

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN')")
    @PutMapping("/{serviceId}/image")
    public ResponseEntity<ServiceSummaryResponse> setImage(@PathVariable UUID serviceId,
                                                           @RequestBody ImageUrlRequest req) {
        var actorId = currentUserService.getCurrentUserId();
        var s = serviceService.updateImage(serviceId, req.url(), actorId);
        return ResponseEntity.ok(serviceMapper.toSummaryResponse(s));
    }
}
