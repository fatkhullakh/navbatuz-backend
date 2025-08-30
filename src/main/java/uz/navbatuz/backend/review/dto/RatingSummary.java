package uz.navbatuz.backend.review.dto;

public record RatingSummary(
        Double average,
        Long count
) {}