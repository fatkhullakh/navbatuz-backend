// src/main/java/uz/navbatuz/backend/receptionist/controller/ReceptionistSelfController.java
package uz.navbatuz.backend.receptionist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.receptionist.dto.ReceptionistDto;
import uz.navbatuz.backend.receptionist.service.ReceptionistService;
import uz.navbatuz.backend.security.CurrentUserService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receptionists")
public class ReceptionistSelfController {
    private final ReceptionistService service;
    private final CurrentUserService currentUserService;

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<ReceptionistDto> me() {
        var uid = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(service.getActiveByUserOrThrow(uid));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @GetMapping("/my-provider")
    public ResponseEntity<Map<String, UUID>> myProvider() {
        var uid = currentUserService.getCurrentUserId();
        var pid = service.providerForUser(uid)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "No active receptionist mapping"));
        return ResponseEntity.ok(Map.of("providerId", pid));
    }
}
