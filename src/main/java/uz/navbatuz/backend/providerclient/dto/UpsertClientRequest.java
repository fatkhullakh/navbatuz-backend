package uz.navbatuz.backend.providerclient.dto;

import java.util.UUID;

public record UpsertClientRequest(
        String name,
        String phone,
        String avatarUrl,
        UUID customerId,   // optional – links existing customer
        UUID guestId       // optional – links existing guest
) {}
