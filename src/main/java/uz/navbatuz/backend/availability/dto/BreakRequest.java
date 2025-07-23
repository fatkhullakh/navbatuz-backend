package uz.navbatuz.backend.availability.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BreakRequest (
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {
    public boolean isValid() {
        return startTime.isBefore(endTime);
    }
}