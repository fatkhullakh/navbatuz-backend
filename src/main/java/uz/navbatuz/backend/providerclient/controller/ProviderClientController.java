// uz.navbatuz.backend.providerclient.controller.ProviderClientController.java
package uz.navbatuz.backend.providerclient.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.providerclient.dto.ProviderClientResponse;
import uz.navbatuz.backend.providerclient.service.ProviderClientService;
import uz.navbatuz.backend.security.AuthorizationService;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.staff.StaffGuard;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/providers/{providerId}/clients")
@PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
public class ProviderClientController {

    private final ProviderClientService service;
    private final AuthorizationService authorizationService;
    private final CurrentUserService currentUserService;
    private final StaffGuard staffGuard;

    private final ProviderClientService clientService;

    @GetMapping("/search")
    public ResponseEntity<List<ProviderClientResponse>> search(
            @PathVariable UUID providerId,
            @RequestParam(defaultValue = "") String q
    ) {
        // authorize: owner OR worker OR receptionist of this provider
        UUID userId = currentUserService.getCurrentUserId();
        staffGuard.ensureStaffOfProvider(userId, providerId);

        return ResponseEntity.ok(
                clientService.search(providerId, q == null ? "" : q.trim())
        );
    }
}
