// src/main/java/uz/navbatuz/backend/receptionist/dto/ReceptionistCreateReq.java
package uz.navbatuz.backend.receptionist.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ReceptionistCreateReq(
        @NotNull UUID userId,
        LocalDate hireDate // optional; defaults to today
) {}
