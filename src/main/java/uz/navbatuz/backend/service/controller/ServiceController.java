package uz.navbatuz.backend.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import uz.navbatuz.backend.service.model.ServiceEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.service.ServiceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceService serviceService;

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceEntity>> getAllByProvider(@PathVariable UUID providerId) {
        return ResponseEntity.ok(serviceService.getAllActiveServicesByProvider(providerId));
    }


    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@RequestBody CreateServiceRequest request) {
        return ResponseEntity.ok(serviceService.createService(request));
    }
}
