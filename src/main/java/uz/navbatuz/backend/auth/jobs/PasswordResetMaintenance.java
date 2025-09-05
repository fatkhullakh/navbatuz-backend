package uz.navbatuz.backend.auth.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.auth.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetMaintenance {

    private final PasswordResetTokenRepository resetRepo;

    // run hourly
    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpired() {
        resetRepo.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
