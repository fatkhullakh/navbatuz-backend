package uz.navbatuz.backend.worker.dto;

import java.util.UUID;

public record WorkerPublicDetailsDto(
        UUID id,
        String name,
        String avatarUrl,
        String status,       // AVAILABLE / UNAVAILABLE / ON_BREAK / ON_LEAVE
        Float avgRating,     // 0..5
        Long reviewsCount    // total reviews
) {}
