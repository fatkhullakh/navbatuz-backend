package uz.navbatuz.backend.provider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.navbatuz.backend.provider.model.BusinessHour;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessHourRepository extends JpaRepository<BusinessHour, UUID> {

    List<BusinessHour> findByProviderId(UUID providerId);

    void deleteByProviderId(UUID providerId);
}
