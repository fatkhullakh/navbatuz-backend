package uz.navbatuz.backend.availability.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.worker.model.Worker;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "actual_availability")
public class ActualAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Worker worker;

    @Column(nullable = false)
    private LocalDate date; // specific date
    // this is used only when the schedule is different from planned availability. Before booking appointment it first
    // checks if there is any actual availability for that date(different one) if not jut go to planned availability
    // do minus breaks and already booked appointments and there u go -> free slots

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private Duration bufferBetweenAppointments;

    public boolean isValid() {
        return startTime.isBefore(endTime);
    }
}
