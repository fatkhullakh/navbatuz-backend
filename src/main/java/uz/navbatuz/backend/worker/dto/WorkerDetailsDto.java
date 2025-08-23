package uz.navbatuz.backend.worker.dto;

import uz.navbatuz.backend.common.Gender;
import uz.navbatuz.backend.common.Status;
import uz.navbatuz.backend.common.WorkerType;

import java.time.LocalDate;
import java.util.UUID;

public record WorkerDetailsDto(
        UUID id,
        String fullName,
        String providerName,
        Gender gender,
        String phoneNumber,
        String email,
        WorkerType workerType,
        Status status,
        float avgRating,
        LocalDate hireDate,
        boolean isActive,
        String avatarUrl
) {
}
