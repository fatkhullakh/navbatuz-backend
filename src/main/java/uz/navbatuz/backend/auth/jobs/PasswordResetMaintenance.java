package uz.navbatuz.backend.auth.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.navbatuz.backend.auth.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetMaintenance {
    private final PasswordResetTokenRepository resetRepo;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // hourly
    public void purgeExpired() {
        resetRepo.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
