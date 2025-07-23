package uz.navbatuz.backend.availability.dto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public record PlannedAvailabilityRequest(
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime,
        Duration bufferBetweenAppointments
) {
    public boolean isValid() {
        return startTime.isBefore(endTime);
    }
}

