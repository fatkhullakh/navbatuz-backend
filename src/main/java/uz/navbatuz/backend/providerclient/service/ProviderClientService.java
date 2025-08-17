// uz.navbatuz.backend.providerclient.service.ProviderClientService.java
package uz.navbatuz.backend.providerclient.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.guest.model.Guest;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.providerclient.dto.ProviderClientResponse;
import uz.navbatuz.backend.providerclient.model.ProviderClient;
import uz.navbatuz.backend.providerclient.repository.ProviderClientRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProviderClientService {
    private final ProviderClientRepository repo;

    private String normalizePhone(String s) {
        if (s == null) return null;
        s = s.replaceAll("[\\s\\-()]", "");
        if (!s.startsWith("+")) s = "+" + s;
        return s;
    }

    private String mask(String e164) {
        if (e164 == null || e164.length() < 4) return "***";
        int n = e164.length();
        return "*".repeat(Math.max(0, n - 4)) + e164.substring(n - 4);
    }

    @Transactional
    public void upsertFromAppointment(Provider provider,
                                      Customer customer, // nullable
                                      Guest guest,       // nullable
                                      String fallbackName,
                                      String fallbackPhone,
                                      UUID actorUserId) {
        final var now = LocalDateTime.now();

        final String phone = normalizePhone(
                customer != null ? customer.getUser().getPhoneNumber()
                        : guest != null ? guest.getPhoneNumber()
                        : fallbackPhone
        );
        if (phone == null || phone.isBlank()) return; // nothing to index

        final String name = (customer != null) ? customer.getUser().getName()
                : (guest != null) ? guest.getName()
                : fallbackName;

        var existing = repo.findByProviderIdAndPhoneE164(provider.getId(), phone)
                .orElse(null);
        if (existing == null) {
            var pc = ProviderClient.builder()
                    .provider(provider)
                    .personType(customer != null ? ProviderClient.PersonType.CUSTOMER
                            : ProviderClient.PersonType.GUEST)
                    .customerId(customer != null ? customer.getId() : null)
                    .guestId(guest != null ? guest.getId() : null)
                    .name(name)
                    .phoneE164(phone)
                    .lastVisitAt(now)
                    .totalVisits(1)
                    .createdAt(now)
                    .createdBy(actorUserId)
                    .build();
            repo.save(pc);
        } else {
            // keep earliest createdAt/createdBy; update linkage/name if better
            if (existing.getCustomerId() == null && customer != null) {
                existing.setPersonType(ProviderClient.PersonType.CUSTOMER);
                existing.setCustomerId(customer.getId());
                existing.setGuestId(null);
            } else if (existing.getGuestId() == null && guest != null) {
                existing.setPersonType(ProviderClient.PersonType.GUEST);
                existing.setGuestId(guest.getId());
            }
            if (name != null && !name.isBlank()) existing.setName(name);
            existing.setLastVisitAt(now);
            existing.setTotalVisits(existing.getTotalVisits() + 1);
            repo.save(existing);
        }
    }

    @Transactional(readOnly = true)
    public List<ProviderClientResponse> search(UUID providerId, String q) {
        final String prefix = normalizePhone(q);
        return repo.searchByPhonePrefix(providerId, prefix).stream()
                .limit(20)
                .map(pc -> new ProviderClientResponse(
                        pc.getId(),
                        pc.getName(),
                        mask(pc.getPhoneE164()),
                        pc.getPersonType().name(),
                        pc.getPersonType() == ProviderClient.PersonType.CUSTOMER ? pc.getCustomerId() : pc.getGuestId()
                ))
                .toList();
    }
}
