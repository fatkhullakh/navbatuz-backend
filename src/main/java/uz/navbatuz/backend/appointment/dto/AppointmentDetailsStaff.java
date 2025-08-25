package uz.navbatuz.backend.appointment.dto;

import uz.navbatuz.backend.common.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentDetailsStaff(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,

        String workerName,
        String providerName,
        String serviceName,
        String customerName,
        String phoneNumber,
        String avatarUrl
) {
}
