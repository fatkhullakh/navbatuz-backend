package uz.navbatuz.backend.provider.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.DayOfWeek;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "business_hour")
public class BusinessHour {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;
}
