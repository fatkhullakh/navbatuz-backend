package uz.navbatuz.backend.service.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uz.navbatuz.backend.provider.dto.ProviderRequest;
import uz.navbatuz.backend.service.model.ServiceEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.service.ServiceService;
import uz.navbatuz.backend.worker.dto.WorkerResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceService serviceService;

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceResponse>> getAllActiveByProvider(@PathVariable UUID providerId) {
        List<ServiceResponse> services = serviceService.getAllActiveServicesByProvider(providerId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<ServiceResponse>> getAllActiveByWorker(@PathVariable UUID workerId) {
        List<ServiceResponse> services = serviceService.getAllActiveServicesByWorker(workerId);
        return ResponseEntity.ok(services);
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@RequestBody CreateServiceRequest request) {
        return ResponseEntity.ok(serviceService.createService(request));
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceService.getService(serviceId));
    }

    @GetMapping("/provider/all/{providerId}")
    public ResponseEntity<List<ServiceResponse>> getAllByProvider(@PathVariable UUID providerId) {
        List<ServiceResponse> services = serviceService.getAllServicesByProvider(providerId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/worker/all/{workerId}")
    public ResponseEntity<List<ServiceResponse>> getAllByWorker(@PathVariable UUID workerId) {
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
    public ResponseEntity<Page<ServiceResponse>> searchServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceResponse> services = serviceService.searchServices(category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(services);
    }






    //
    // Search/filter by category, price range, duration, etc.
    // connect worker to service with many-to-many relationship
}
