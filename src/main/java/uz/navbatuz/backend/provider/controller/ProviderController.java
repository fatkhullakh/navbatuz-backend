package uz.navbatuz.backend.provider.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.dto.*;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.service.ProviderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/public/register")
    public ResponseEntity<ProviderResponse> create(@RequestBody @Valid ProviderRequest request) {
        Provider provider = providerService.create(request);  // If exception happens, global handler catches it
        return ResponseEntity.ok(new ProviderResponse(
                provider.getName(),
                provider.getDescription(),
                provider.getAvgRating()
        ));
    }

//    {
//        "name": "BarberPro",
//            "description": "Best barbers in Tashkent",
//            "category": "Barber",
//            "teamSize": 5,
//            "email": "barberproad@example.com",
//            "phoneNumber": "+998901234522",
//            "ownerId": "50017837-7163-452d-87a8-fc8ed8b88d46"
//    }
//{
//    "ownerId": "7e8afd33-b128-46bc-af5e-1ef2f963c7bb",
//        "name": "Prestige Barber",
//        "description": "Modern barbershop in the city center",
//        "category": "BARBERSHOP",
//        "email": "prestige@barber.uz",
//        "phoneNumber": "+998991234567",
//        "teamSize": 5
//}


    @GetMapping("/public/{id}")
    public ResponseEntity<ProvidersDetails> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getById(id));
    }

//    @GetMapping
//    public ResponseEntity<List<ProviderResponse>> getAllActiveProviders() {
//        return ResponseEntity.ok(providerService.getAllActiveProviders());
//    }

    @GetMapping("/public/all")
    public ResponseEntity<Page<ProviderResponse>> getAllActiveProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(providerService.getAllActiveProviders(pageable));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<ProviderResponse>> getAllProviders() {
        return ResponseEntity.ok(providerService.getAllProviders());
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST')")
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody ProviderRequest request) {
        providerService.updateById(id, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST')")
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        providerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<ProviderResponse>> searchProviders(
            @RequestParam Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                providerService.searchByCategory(category, pageable)
        );
    }

    @GetMapping("/public/{providerId}/business-hours")
    public ResponseEntity<List<BusinessHourResponse>> getBusinessHours(@PathVariable UUID providerId) {
        return ResponseEntity.ok(providerService.getBusinessHours(providerId));
    }

    @PostMapping("/{providerId}/business-hours")
    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Void> setBusinessHours(@PathVariable UUID providerId, @RequestBody List<BusinessHourRequest> request) {
        providerService.setBusinessHours(providerId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{providerId}/business-hours")
    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Void> updateBusinessHours(@PathVariable UUID providerId, @RequestBody List<BusinessHourRequest> request) {
        providerService.updateBusinessHours(providerId, request);
        return ResponseEntity.ok().build();
    }


    // localhost:8080/api/providers/public/search?category=CLINIC&page=0&size=5


}
