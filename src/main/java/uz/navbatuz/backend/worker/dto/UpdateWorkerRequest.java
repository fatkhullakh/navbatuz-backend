package uz.navbatuz.backend.worker.dto;

import jakarta.annotation.Nullable;
import uz.navbatuz.backend.common.Gender;
import uz.navbatuz.backend.common.Status;
import uz.navbatuz.backend.common.WorkerType;

public record UpdateWorkerRequest(
        @Nullable String name,          // user's first name
        @Nullable String surname,       // user's last name
        @Nullable Gender gender,
        @Nullable String phoneNumber,
        @Nullable String email,
        @Nullable WorkerType workerType,
        @Nullable Status status,
        @Nullable Boolean isActive,
        @Nullable String avatarUrl
) {}
