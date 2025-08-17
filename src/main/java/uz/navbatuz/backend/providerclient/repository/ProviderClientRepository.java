// uz.navbatuz.backend.providerclient.repository.ProviderClientRepository.java
package uz.navbatuz.backend.providerclient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.navbatuz.backend.providerclient.model.ProviderClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderClientRepository extends JpaRepository<ProviderClient, UUID> {

    Optional<ProviderClient> findByProviderIdAndPhoneE164(UUID providerId, String phoneE164);

    @Query("""
       select pc
       from ProviderClient pc
       where pc.provider.id = :providerId
         and pc.phoneE164 like concat(:prefix, '%')
       order by pc.lastVisitAt desc
    """)
    List<ProviderClient> searchByPhonePrefix(UUID providerId, String prefix);
}
