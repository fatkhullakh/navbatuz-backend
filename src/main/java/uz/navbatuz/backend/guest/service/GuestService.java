package uz.navbatuz.backend.guest.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.guest.dto.GuestResponse;
import uz.navbatuz.backend.guest.model.Guest;
import uz.navbatuz.backend.guest.repository.GuestRepository;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestService {
    private final GuestRepository guestRepo;
    private final ProviderRepository providerRepo;

    @Transactional
    public Guest findOrCreate(UUID providerId, String phoneE164, String name, UUID createdBy, String createdByRole) {
        Provider p = providerRepo.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        return guestRepo.findByProviderIdAndPhoneNumber(providerId, phoneE164)
                .map(g -> {
                    g.setLastSeenAt(LocalDateTime.now());
                    if (g.getName() == null && name != null && !name.isBlank()) g.setName(name);
                    return g;
                })
                .orElseGet(() -> guestRepo.save(Guest.builder()
                        .id(null)
                        .provider(p)
                        .phoneNumber(phoneE164)
                        .name(name)
                        .createdAt(LocalDateTime.now())
                        .lastSeenAt(LocalDateTime.now())
                        .createdByUser(createdBy)
                        .build()
                ));
    }

    private String normalizePhone(String s) {
        if (s == null) return null;
        s = s.replaceAll("[\\s\\-()]", "");
        // if you always store E.164, force leading '+'
        if (!s.startsWith("+")) s = "+" + s;
        return s;
    }

    private String mask(String e164) {
        if (e164 == null || e164.length() < 4) return "***";
        int n = e164.length();
        return "*".repeat(Math.max(0, n - 4)) + e164.substring(n - 4);
    }

    public List<GuestResponse> searchByPhonePrefixGlobal(String rawPrefix) {
        String prefix = normalizePhone(rawPrefix);
        return guestRepo.searchByPhonePrefixGlobal(prefix).stream()
                .limit(10)
                .map(g -> new GuestResponse(g.getId(), g.getName(), mask(g.getPhoneNumber())))
                .toList();
    }

    public GuestResponse getOne(UUID providerId, String phoneE164) {
        var g = guestRepo.findByProviderIdAndPhoneNumber(providerId, phoneE164)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
        return new GuestResponse(g.getId(), g.getName(), mask(g.getPhoneNumber()));
    }
}
