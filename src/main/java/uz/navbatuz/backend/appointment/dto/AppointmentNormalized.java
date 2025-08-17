package uz.navbatuz.backend.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentNormalized(
        UUID workerId, UUID serviceId, LocalDate date, LocalTime startTime,
        UUID customerId, UUID guestId
) {}
