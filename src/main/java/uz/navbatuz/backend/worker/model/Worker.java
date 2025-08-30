package uz.navbatuz.backend.worker.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.common.Status;
import uz.navbatuz.backend.common.WorkerType;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.user.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workers")
public class Worker {
    @Id
    private UUID id;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkerType workerType;

    private Status status;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private Float avgRating;
    private Long reviewsCount;
    private boolean isActive;

    @ManyToMany(mappedBy = "workers")
    private List<ServiceEntity> services;
}
