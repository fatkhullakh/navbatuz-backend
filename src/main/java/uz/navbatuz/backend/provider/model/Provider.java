package uz.navbatuz.backend.provider.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.location.model.Location;
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
@Table (name = "providers")
public class Provider {

    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    private String name;

    @Column(length = 2000)
    private @Size(max = 2000)String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;
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
    private Float avgRating;
    private Long reviewsCount;
    boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", updatable = false)
    private User owner;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    @Column(name = "logo_url")
    private String logoUrl;

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    @Column(name = "min_advance_booking_minutes")
    private Integer minAdvanceBookingMinutes;

    // TODO: Relationship Provider with Receptionist and hasReceptinist method
//    public boolean hasReceptionist(User currentUser) {
//
//    }
}
