package uz.navbatuz.backend.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import uz.navbatuz.backend.common.Category;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record CreateServiceRequest(
        String name,
        String description,
        Category category,
        Double price,
        Duration duration,
        UUID providerId,
        List<UUID> workerIds,
        String imageUrl
) {}

