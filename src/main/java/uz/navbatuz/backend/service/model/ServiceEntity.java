package uz.navbatuz.backend.service.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.worker.model.Worker;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "services")
public class ServiceEntity {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private BigDecimal price;
    private int duration;
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

}
