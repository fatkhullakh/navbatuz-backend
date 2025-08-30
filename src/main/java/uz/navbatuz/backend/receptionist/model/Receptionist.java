// src/main/java/uz/navbatuz/backend/receptionist/model/Receptionist.java
package uz.navbatuz.backend.receptionist.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.user.model.User;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "receptionists")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Receptionist {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReceptionistStatus status;

    // IMPORTANT: map to existing NOT NULL column
    @Column(name = "is_active")
    private boolean active;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Version
    @Column(name = "version")
    private Long version;

    public enum ReceptionistStatus {
        ACTIVE, SUSPENDED, TERMINATED
    }
}
