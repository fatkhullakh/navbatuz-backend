package uz.navbatuz.backend.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.user.service.AccountRestoreService;
import uz.navbatuz.backend.user.service.SoftDeleteService;

import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class AccountController {

    private final SoftDeleteService softDeleteService;

    public record DeleteReq(String reason) {}

    @PostMapping("/delete")
    public ResponseEntity<?> deleteMyAccount(@RequestBody(required = false) DeleteReq req,
                                             HttpServletRequest request) {
        final String reason = (req != null && req.reason() != null) ? req.reason() : "";
        softDeleteService.softDeleteMe(reason, request);
        return ResponseEntity.noContent().build();
    }
}


@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
class AdminAccountController {

    private final AccountRestoreService restoreService;

    @PostMapping("/{userId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restore(@PathVariable UUID userId) {
        restoreService.restore(userId);
        return ResponseEntity.noContent().build();
    }
}
