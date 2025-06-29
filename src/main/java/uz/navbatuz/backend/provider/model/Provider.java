package uz.navbatuz.backend.provider.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import uz.navbatuz.backend.user.model.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table (name = "provider")
public class Provider {

    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String description;
    private String category;
    private int teamSize;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;
    @Column(updatable = false)
    private LocalDateTime data_created;

    private LocalDateTime data_updated;

    @PrePersist
    public void prePersist() {
        data_created = LocalDateTime.now();
        data_updated = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        data_updated = LocalDateTime.now();
    }
    private float avgRating;
    boolean isActive;

    @Column(nullable = false, updatable = false)
    private UUID ownerId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ownerId")
//    private User owner;
}
