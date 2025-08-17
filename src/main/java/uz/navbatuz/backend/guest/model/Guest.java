package uz.navbatuz.backend.guest.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.provider.model.Provider;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "guests",
        uniqueConstraints = @UniqueConstraint(name = "uk_guest_provider_phone", columnNames = {"provider_id", "phone_number"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Guest {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;           // store E.164

    @Column(name = "name")
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "created_by_user")
    private UUID createdByUser;           // users.id

}
