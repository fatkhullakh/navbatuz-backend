package uz.navbatuz.backend.provider.dto;

import java.time.LocalTime;
import java.time.DayOfWeek;

public record BusinessHourResponse(
        Long id,
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime
) {}
