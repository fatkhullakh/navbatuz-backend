package uz.navbatuz.backend.worker.dto;

import uz.navbatuz.backend.common.Status;
import uz.navbatuz.backend.common.WorkerType;

import java.time.LocalDate;
import java.util.UUID;

public record WorkerResponse(
        UUID id,
        String fullName,
        String providerName,
        WorkerType workerType,
        Status status,
        float avgRating,
        LocalDate hireDate,
        boolean isActive
) {}
