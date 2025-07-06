package uz.navbatuz.backend.provider.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.provider.dto.ProviderRequest;
import uz.navbatuz.backend.provider.dto.ProviderResponse;
import uz.navbatuz.backend.provider.dto.ProvidersDetails;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.service.ProviderService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/register")
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


    @GetMapping("/{id}")
    public ResponseEntity<ProvidersDetails> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getById(id));
    }

//    @GetMapping
//    public ResponseEntity<List<ProviderResponse>> getAllActiveProviders() {
//        return ResponseEntity.ok(providerService.getAllActiveProviders());
//    }

    @GetMapping
    public ResponseEntity<Page<ProviderResponse>> getAllActiveProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(providerService.getAllActiveProviders(pageable));
    }


    @GetMapping("/admin")
    public ResponseEntity<List<ProviderResponse>> getAllProviders() {
        return ResponseEntity.ok(providerService.getAllProviders());
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody ProviderRequest request) {
        providerService.updateById(id, request);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        providerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProviderResponse>> searchProviders(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                providerService.searchByCategory(category, pageable)
        );
    }


}
