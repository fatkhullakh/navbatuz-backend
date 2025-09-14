// src/main/java/uz/navbatuz/backend/user/service/AccountRestoreService.java
package uz.navbatuz.backend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.user.model.DeletedUserArchive;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.DeletedUserArchiveRepository;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountRestoreService {
    private final UserRepository users;
    private final DeletedUserArchiveRepository archives;
    private final PasswordEncoder encoder;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void restore(UUID userId) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        DeletedUserArchive a = archives.findByOriginalUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Archive not found"));

        // If email was reused by someone else, admin must set a new one before restore.
        if (a.getEmail() != null && users.existsByEmail(a.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email is already taken; set a new email before restoring");
        }

        u.setActive(true);
        u.setDeletedAt(null);
        u.setEmail(a.getEmail());
        u.setPhoneNumber(a.getPhoneNumber());
        u.setName(a.getName());
        u.setSurname(a.getSurname());
        u.setAvatarUrl(a.getAvatarUrl());

        // You can restore gender/language/role if needed:
        try {
            if (a.getGender() != null) u.setGender(a.getGender());
        } catch (Exception ignored) {}
        try {
            if (a.getLanguage() != null) u.setLanguage(a.getLanguage());
        } catch (Exception ignored) {}
        try {
            if (a.getRole() != null) u.setRole(a.getRole());
        } catch (Exception ignored) {}

        // force password reset: set random hash; user must reset via “Forgot password”
        u.setPasswordHash(encoder.encode("restored:" + UUID.randomUUID()));

        users.save(u);

        // Optional: keep archive row for audit, or delete it after restore:
        // archives.delete(a);
    }
}
