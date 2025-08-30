// src/main/java/uz/navbatuz/backend/receptionist/controller/ReceptionistController.java
package uz.navbatuz.backend.receptionist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.receptionist.dto.ReceptionistCreateReq;
import uz.navbatuz.backend.receptionist.dto.ReceptionistDetailsDto;
import uz.navbatuz.backend.receptionist.dto.ReceptionistDto;
import uz.navbatuz.backend.receptionist.dto.ReceptionistUpdateReq;
import uz.navbatuz.backend.receptionist.model.Receptionist;
import uz.navbatuz.backend.receptionist.service.ReceptionistService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers/{providerId}/receptionists")
@RequiredArgsConstructor
public class ReceptionistController {

    private final ReceptionistService service;

    // Keep the same broad guard style you used on Worker; the service enforces OWNER.
    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
    @PostMapping
    public ResponseEntity<ReceptionistDto> create(@PathVariable UUID providerId,
                                                  @RequestBody @Valid ReceptionistCreateReq req) {
        Receptionist r = service.createReceptionist(providerId, req);
        return ResponseEntity.ok(new ReceptionistDto(
                r.getId(), r.getProvider().getId(), r.getUser().getId(),
                r.getStatus(), r.isActive(), r.getHireDate(), r.getTerminationDate(), r.getVersion()
        ));
    }

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','ADMIN')")
    @GetMapping
    public ResponseEntity<List<ReceptionistDetailsDto>> list(@PathVariable UUID providerId) {
        return ResponseEntity.ok(service.list(providerId));
    }

    @PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','ADMIN')")
    @GetMapping("/{receptionistId}")
    public ResponseEntity<ReceptionistDetailsDto> getById(@PathVariable UUID receptionistId) {
        return ResponseEntity.ok(service.getById(receptionistId));
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PutMapping("/{receptionistId}/deactivate")
    public ResponseEntity<ReceptionistDetailsDto> deactivate(@PathVariable UUID providerId,
                                                             @PathVariable UUID receptionistId) {
        return ResponseEntity.ok(service.deactivate(providerId, receptionistId));
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PutMapping("/{receptionistId}/activate")
    public ResponseEntity<ReceptionistDetailsDto> activate(@PathVariable UUID providerId,
                                                           @PathVariable UUID receptionistId) {
        return ResponseEntity.ok(service.activate(providerId, receptionistId));
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PatchMapping("/{receptionistId}")
    public ResponseEntity<ReceptionistDetailsDto> update(@PathVariable UUID providerId,
                                                         @PathVariable UUID receptionistId,
                                                         @RequestBody ReceptionistUpdateReq req) {
        return ResponseEntity.ok(service.update(providerId, receptionistId, req));
    }
}
