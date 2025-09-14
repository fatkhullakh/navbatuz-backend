package uz.navbatuz.backend.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.DeletedUserArchive;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.DeletedUserArchiveRepository;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SoftDeleteService {

    private final CurrentUserService current;
    private final UserRepository users;
    private final DeletedUserArchiveRepository archiveRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public void softDeleteMe(String reason, HttpServletRequest request) {
        UUID userId = current.getCurrentUserId();
        User u = users.findById(userId).orElseThrow();

        // 1) Save snapshot to archive (plain table, no json)
        DeletedUserArchive row = DeletedUserArchive.builder()
                .originalUserId(u.getId())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .name(u.getName())
                .surname(u.getSurname())
                .gender(u.getGender())
                .dateOfBirth(u.getDateOfBirth())
                .language(u.getLanguage())
                .role(u.getRole())
                .country(u.getCountry())
                .avatarUrl(u.getAvatarUrl())
                .deletedAt(LocalDateTime.now())
                .deletedByUserId(userId)
                .deletedByIp(extractIp(request))
                .reason(reason)
                .build();
        archiveRepo.save(row);

        // 2) Scrub + deactivate the live user
        String marker = UUID.randomUUID().toString().replace("-", "");
        String scrubEmail = "deleted+" + marker + "@birzum.app";
        String scrubPhone = "deleted+" + marker; // User.phoneNumber is NOT NULL + UNIQUE

        u.setActive(false);
        u.setEmail(scrubEmail);
        u.setPhoneNumber(scrubPhone);
        u.setName(null);
        u.setSurname(null);
        u.setAvatarUrl(null);
        u.setPasswordHash(encoder.encode(marker)); // invalidate any sessions
        u.setDeletedAt(LocalDateTime.now());

        users.save(u);

        // If you keep token storage/refresh tokens, revoke here.
        // jwtService.revokeAll(u);
    }

    private String extractIp(HttpServletRequest req) {
        if (req == null) return null;
        String fwd = req.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            int comma = fwd.indexOf(',');
            return comma > 0 ? fwd.substring(0, comma).trim() : fwd.trim();
        }
        return req.getRemoteAddr();
    }
}
