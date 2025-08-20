package uz.navbatuz.backend.guest.dto;

import java.util.UUID;

public record GuestLookupCreateRequest(
        UUID workerId,
        String phoneE164,
        String name
) {}