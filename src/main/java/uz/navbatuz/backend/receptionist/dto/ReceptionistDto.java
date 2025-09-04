// src/main/java/uz/navbatuz/backend/receptionist/dto/ReceptionistDto.java
package uz.navbatuz.backend.receptionist.dto;

import uz.navbatuz.backend.receptionist.model.Receptionist;

import java.time.LocalDate;
import java.util.UUID;

public record ReceptionistDto(
        UUID id,
        UUID providerId,
        UUID userId,
        Receptionist.ReceptionistStatus status,
        boolean isActive,
        LocalDate hireDate,
        LocalDate terminationDate,
        Long version
) {}



