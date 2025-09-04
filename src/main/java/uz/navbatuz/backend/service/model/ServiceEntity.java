package uz.navbatuz.backend.service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.worker.model.Worker;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "services")
@SQLDelete(sql = "UPDATE services SET deleted = true, is_active = false WHERE id = ?")
public class ServiceEntity {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private BigDecimal price;
    private Duration duration;
    private boolean isActive;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "worker_services",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "worker_id")
    )
    private List<Worker> workers;
}

// TODO: FEATURE: Remove worker from service
//  Customer view endpoints
//  e.g. services by city, trending services, etc.
