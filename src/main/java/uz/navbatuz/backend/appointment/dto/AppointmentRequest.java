package uz.navbatuz.backend.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentRequest (
        UUID workerId,
        UUID serviceId,
        LocalDate date,
        LocalTime startTime,

        UUID customerId, //nullable
        UUID guestId,      // staff: reuse existing guest
        String guestPhone, // staff: create/reuse by phone (provider-scoped)
        String guestName
){
}
