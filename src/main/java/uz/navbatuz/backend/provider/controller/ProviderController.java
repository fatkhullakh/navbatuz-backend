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
import uz.navbatuz.backend.location.dto.LocationRequest;
import uz.navbatuz.backend.location.dto.LocationResponse;
import uz.navbatuz.backend.location.dto.LocationSummary;
import uz.navbatuz.backend.location.model.Location;
import uz.navbatuz.backend.provider.dto.*;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.service.ProviderService;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.worker.dto.WorkerDetailsDto;
import uz.navbatuz.backend.worker.service.WorkerService;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    private final CurrentUserService currentUserService;

    private static LocationSummary toSummary(uz.navbatuz.backend.location.model.Location loc) {
        if (loc == null) return null;
        return new LocationSummary(
                loc.getId(),
                loc.getAddressLine1(),
                loc.getCity(),
                loc.getCountryIso2()
        );
    }

    @PostMapping("/public/register")
    public ResponseEntity<ProviderResponse> create(@RequestBody @Valid ProviderRequest request) {
        Provider p = providerService.create(request);
        return ResponseEntity.ok(new ProviderResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getAvgRating(),
                p.getCategory(),
                toSummary(p.getLocation()), // <-- NULL-SAFE
                p.getLogoUrl()
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
    public ResponseEntity<ProviderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getById(id));
    }

    @GetMapping("/public/{id}/details")
    public ResponseEntity<ProvidersDetails> getProvidersDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getProvidersDetails(id));
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

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'ADMIN')")
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @Valid @RequestBody ProviderUpdateRequest request) {
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

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'ADMIN')")
    @PutMapping("/{providerId}/location")
    public ResponseEntity<Void> updateLocation(@PathVariable UUID providerId, @RequestBody LocationRequest location) {
        providerService.updateLocation(providerId, location);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/{providerId}/location")
    public ResponseEntity<LocationSummary> getLocationSummary(@PathVariable UUID providerId) {
        return ResponseEntity.ok(providerService.getLocationSummary(providerId));
    }

    @GetMapping("/admin/{providerId}/location")
    public ResponseEntity<LocationResponse> getLocationDetails(@PathVariable UUID providerId) {
        return ResponseEntity.ok(providerService.getLocation(providerId));
    }

    @GetMapping
    public List<Map<String,String>> list() {
        return Arrays.stream(Category.values())
                .map(c -> Map.of("id", c.name(), "name", c.name().replace("_"," ").toLowerCase()))
                .toList();
    }

    record ImageUrlRequest(String url) {}

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PutMapping("/{providerId}/logo")
    public ResponseEntity<ProviderResponse> setLogo(
            @PathVariable UUID providerId,
            @RequestBody ImageUrlRequest req
    ) {
        var actorId = currentUserService.getCurrentUserId();
        var p = providerService.updateLogo(providerId, req.url(), actorId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER')")
    @GetMapping("/me")
    public ResponseEntity<ProviderResponse> getMyProvider() {
        var userId = currentUserService.getCurrentUserId();
        var role = currentUserService.getCurrentUserRole(); // implement this in CurrentUserService
        var providerId = providerService.getProviderIdForUser(userId, role);
        return ResponseEntity.ok(providerService.getById(providerId));
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/{providerId}/owner-as-worker")
    public ResponseEntity<WorkerDetailsDto> enableOwnerAsWorker(
            @PathVariable UUID providerId,
            @RequestBody EnableOwnerAsWorkerRequest req
    ) {
        return ResponseEntity.ok(providerService.enable(providerId, req));
    }



//    @PutMapping("/{id}/logo")
//    @PreAuthorize("hasAnyRole('OWNER','ADMIN')") // adjust roles as needed
//    public ResponseEntity<Void> setLogo(
//            @PathVariable UUID id,
//            @RequestBody Map<String, String> body
//            /*, Authentication auth */
//    ) {
//        var url = Objects.toString(body.get("url"), "");
//        if (url.isBlank()) return ResponseEntity.badRequest().build();
//
//        providerService.setLogoUrl(id, url /*, auth.getName() */);
//        return ResponseEntity.noContent().build();
//    }


    // localhost:8080/api/providers/public/search?category=CLINIC&page=0&size=5



}
