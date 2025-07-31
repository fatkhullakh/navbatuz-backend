package uz.navbatuz.backend.provider.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record BusinessHourRequest(
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime
) {
    public boolean isValid() {
        return startTime.isBefore(endTime);
    }
}
