package uz.navbatuz.backend.availability.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ActualAvailabilityResponse(
        Long id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}
