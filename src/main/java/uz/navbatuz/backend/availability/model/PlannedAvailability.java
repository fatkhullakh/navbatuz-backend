package uz.navbatuz.backend.availability.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.worker.model.Worker;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "planned_availability")
public class PlannedAvailability {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek day;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private Duration bufferBetweenAppointments;

    public boolean isValid() {
        return startTime.isBefore(endTime);
    }
}
