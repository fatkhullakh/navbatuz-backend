package uz.navbatuz.backend.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import uz.navbatuz.backend.common.Category;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record CreateServiceRequest(
        @NotNull String name,
        @NotNull String description,
        @NotNull Category category,
        @Positive Double price,
        @Positive Duration duration,
        @NotNull UUID providerId,
        @NotNull List<UUID> workerIds,
        String imageUrl
) {}

