package uz.navbatuz.backend.auth.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.user.model.User;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false, length = 120)
    private String codeHash;           // BCrypt of 6-digit code

    @Column(nullable = false)
    private LocalDateTime expiresAt;   // e.g., +15 minutes

    @Column(nullable = false)
    private boolean used = false;
}
