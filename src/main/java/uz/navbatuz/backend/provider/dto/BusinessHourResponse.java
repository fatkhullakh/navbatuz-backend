package uz.navbatuz.backend.provider.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record BusinessHourResponse(
        Long id,
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime
) {
}
