// uz.navbatuz.backend.providerclient.model.ProviderClient.java
package uz.navbatuz.backend.providerclient.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.provider.model.Provider;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider_clients",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider_id","phone_e164"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProviderClient {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false)
    private PersonType personType; // CUSTOMER or GUEST

    @Column(name = "customer_id")
    private UUID customerId;       // nullable

    @Column(name = "guest_id")
    private UUID guestId;          // nullable

    @Column(name = "name", length = 200)
    private String name;           // best-known display name

    @Column(name = "phone_e164", nullable = false, length = 32)
    private String phoneE164;      // normalized +998...

    @Column(name = "last_visit_at", nullable = false)
    private LocalDateTime lastVisitAt;

    @Column(name = "total_visits", nullable = false)
    private int totalVisits;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    public enum PersonType { CUSTOMER, GUEST }
}
