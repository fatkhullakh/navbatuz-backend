package uz.navbatuz.backend.guest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.guest.dto.GuestLookupCreateRequest;
import uz.navbatuz.backend.guest.dto.GuestResponse;
import uz.navbatuz.backend.guest.service.GuestService;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.worker.service.WorkerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/guests")
@PreAuthorize("hasAnyRole('OWNER','RECEPTIONIST','WORKER','ADMIN')")
public class GuestController {

    private final GuestService guestService;
    private final WorkerService workerService;
    private final CurrentUserService currentUserService;

    // GLOBAL phone search (no workerId/provider filtering)
    @GetMapping("/search")
    public List<GuestResponse> search(@RequestParam String q) {
        return guestService.searchByPhonePrefixGlobal(q);
    }

    // unchanged: creation still needs provider (via worker)
    @PostMapping("/lookup-or-create")
    public GuestResponse lookupOrCreate(@RequestBody GuestLookupCreateRequest req, Authentication auth) {
        var providerId = workerService.requireProviderId(req.workerId());
        var actorUserId = currentUserService.getCurrentUserId();
        var role = auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");

        var g = guestService.findOrCreate(providerId, req.phoneE164(), req.name(), actorUserId, role);
        return new GuestResponse(g.getId(), g.getName(),
                g.getPhoneNumber() == null ? "***" :
                        "*".repeat(Math.max(0, g.getPhoneNumber().length() - 4)) + g.getPhoneNumber().substring(g.getPhoneNumber().length() - 4));
    }

    private String maskPhone(String e164) {
        if (e164 == null || e164.length() < 4) return "***";
        int n = e164.length();
        // keep last 4, mask the rest
        String last4 = e164.substring(n - 4);
        return "*".repeat(n - 4) + last4;
    }
}
