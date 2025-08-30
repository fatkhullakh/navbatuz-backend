package uz.navbatuz.backend.review.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.worker.model.Worker;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "public_visible", nullable = false)
    private boolean publicVisible = true;   // <— important
}
