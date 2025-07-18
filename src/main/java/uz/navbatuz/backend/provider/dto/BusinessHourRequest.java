package uz.navbatuz.backend.provider.dto;

import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.UUID;

public record BusinessHourRequest(
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime
) {
    public boolean isValid() {
        return startTime != null && endTime != null && endTime.isAfter(startTime);
    }
}
