package uz.navbatuz.backend.appointment.dto;

import uz.navbatuz.backend.common.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse (
        UUID id,
        UUID workerId,
        UUID serviceId,
        UUID customerId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status
){
}
