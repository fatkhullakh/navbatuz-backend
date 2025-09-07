package uz.navbatuz.backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.auth.model.PasswordResetToken;
import uz.navbatuz.backend.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    void deleteByUserAndExpiresAtBefore(User user, LocalDateTime cutoff);
    List<PasswordResetToken> findByUserAndUsedFalseOrderByExpiresAtDesc(User user);
    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
