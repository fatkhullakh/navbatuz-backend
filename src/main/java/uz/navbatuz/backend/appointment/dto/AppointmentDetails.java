package uz.navbatuz.backend.appointment.dto;

import uz.navbatuz.backend.common.AppointmentStatus;
import uz.navbatuz.backend.location.model.Location;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentDetails(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,

        String providerName,
        String addressLine1,
        String city,
        String countryIso2,
        String serviceName,
        BigDecimal price,
        String workerName
) {
}