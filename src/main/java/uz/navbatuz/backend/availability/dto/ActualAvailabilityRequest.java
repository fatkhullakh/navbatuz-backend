package uz.navbatuz.backend.availability.dto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public record ActualAvailabilityRequest (
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Duration bufferBetweenAppointments
) {
        public boolean isValid() {
            return startTime.isBefore(endTime);
        }
}
