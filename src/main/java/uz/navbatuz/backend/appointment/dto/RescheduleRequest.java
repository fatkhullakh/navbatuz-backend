package uz.navbatuz.backend.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleRequest(
        LocalDate newDate,
        LocalTime newStartTime
) {}
