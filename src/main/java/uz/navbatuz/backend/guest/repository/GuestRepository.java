package uz.navbatuz.backend.guest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.navbatuz.backend.guest.model.Guest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuestRepository extends JpaRepository<Guest, UUID> {
    Optional<Guest> findByProviderIdAndPhoneNumber(UUID providerId, String phone);
    boolean existsByProviderIdAndPhoneNumber(UUID providerId, String phone);
    @Query("""
         select g
         from Guest g
         where g.phoneNumber like concat(:prefix, '%')
         order by g.lastSeenAt desc
         """)
    List<Guest> searchByPhonePrefixGlobal(@Param("prefix") String prefix);
}
