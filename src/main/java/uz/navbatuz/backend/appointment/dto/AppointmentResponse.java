package uz.navbatuz.backend.appointment.dto;

import uz.navbatuz.backend.common.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse (
        UUID id,
        UUID workerId,
        UUID serviceId,
        UUID providerId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,

        UUID customerId,
        UUID guestId,
        String guestNameMasked
){
}
