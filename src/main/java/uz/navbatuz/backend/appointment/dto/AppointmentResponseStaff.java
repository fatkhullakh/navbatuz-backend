package uz.navbatuz.backend.appointment.dto;

import uz.navbatuz.backend.common.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponseStaff(
        UUID id,
        UUID workerId,
        UUID providerId,
        UUID serviceId,

        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,

        String workerName,
        String providerName,
        String serviceName,
        String customerName,   // customer.user.name OR guest.name OR masked phone
        String guestMask
) {
}
