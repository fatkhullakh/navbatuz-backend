package uz.navbatuz.backend.appointment.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.common.AppointmentStatus;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.worker.model.Worker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "appointments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"worker_id", "date", "start_time"})
)
public class Appointment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @ManyToOne(optional = true)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    private LocalDateTime bookedDate;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private uz.navbatuz.backend.guest.model.Guest guest; // nullable

    @Column(name = "created_by_user")
    private java.util.UUID createdByUser;

}
