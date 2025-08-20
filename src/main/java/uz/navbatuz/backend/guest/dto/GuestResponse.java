package uz.navbatuz.backend.guest.dto;

import java.util.UUID;

public record GuestResponse(
        UUID id,
        String name,
        String phoneMasked
) {}
