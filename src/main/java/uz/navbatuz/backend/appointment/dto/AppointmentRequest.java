package uz.navbatuz.backend.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentRequest (
        UUID workerId,
        UUID serviceId,
        UUID customerId,
        LocalDate date,
        LocalTime startTime
){
}
