// uz.navbatuz.backend.providerclient.dto.ProviderClientResponse.java
package uz.navbatuz.backend.providerclient.dto;

import uz.navbatuz.backend.providerclient.model.ProviderClient;

import java.util.UUID;

public record ProviderClientResponse(
        UUID id,
        String name,
        String phoneMasked,
        String personType,
        UUID linkId // customerId or guestId, depending on personType
) {}
