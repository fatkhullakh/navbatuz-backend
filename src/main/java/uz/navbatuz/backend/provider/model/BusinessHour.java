package uz.navbatuz.backend.provider.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.DayOfWeek;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "business_hour")
public class BusinessHour {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek day;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    public boolean isValid() {
        return startTime.isAfter(endTime);
    }
}
