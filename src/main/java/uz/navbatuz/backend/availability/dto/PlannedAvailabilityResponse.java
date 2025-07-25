package uz.navbatuz.backend.availability.dto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;

public record PlannedAvailabilityResponse(
        Long id,
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime,
        Duration bufferBetweenAppointments
) {}

