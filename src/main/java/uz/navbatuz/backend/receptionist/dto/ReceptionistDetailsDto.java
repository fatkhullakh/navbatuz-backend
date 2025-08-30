package uz.navbatuz.backend.receptionist.dto;

import uz.navbatuz.backend.common.Gender;
import uz.navbatuz.backend.common.Status;
import uz.navbatuz.backend.common.WorkerType;

import java.time.LocalDate;
import java.util.UUID;

public record ReceptionistDetailsDto(
        UUID id,
        String Name,
        String Surname,
        String providerName,
        Gender gender,
        String phoneNumber,
        String email,
        LocalDate hireDate,
        boolean isActive,
        String avatarUrl
) {
}
