package uz.navbatuz.backend.review.dto;

import java.util.UUID;

public record CreateReviewRequest(
        UUID appointmentId,
        Integer rating,
        String comment
) {}