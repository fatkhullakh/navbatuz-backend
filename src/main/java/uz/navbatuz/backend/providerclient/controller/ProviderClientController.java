// uz.navbatuz.backend.providerclient.controller.ProviderClientController.java
package uz.navbatuz.backend.providerclient.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.providerclient.dto.ProviderClientResponse;
import uz.navbatuz.backend.providerclient.service.ProviderClientService;
import uz.navbatuz.backend.security.AuthorizationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/providers/{providerId}/clients")
@PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
public class ProviderClientController {

    private final ProviderClientService service;
    private final AuthorizationService authorizationService;

    @GetMapping("/search")
    public List<ProviderClientResponse> search(@PathVariable UUID providerId, @RequestParam String q) {
        // Ensure the caller is staff of this provider (implement in your AuthorizationService)
        authorizationService.ensureStaffOfProvider(providerId);
        return service.search(providerId, q);
    }
}
