package uz.navbatuz.backend.review.dto;

import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID appointmentId,
        UUID providerId,
        UUID workerId,
        Integer rating,
        String comment,
        java.time.Instant createdAt,
        String authorName    // simple for UI
) {}
