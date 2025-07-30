package uz.navbatuz.backend.appointment.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.common.AppointmentStatus;
import uz.navbatuz.backend.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentStatusHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus newStatus;

    private LocalDateTime changedAt;

    // Who changed it (customer, worker, receptionist, provider, admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;
}
