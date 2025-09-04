package uz.navbatuz.backend.providerclient.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProviderClientDto(
        UUID id,
        UUID providerId,
        String name,
        String phone,
        String avatarUrl,
        UUID customerId,
        UUID guestId,
        Integer visitCount,
        LocalDateTime lastVisitAt,
        Boolean blocked
) {}
