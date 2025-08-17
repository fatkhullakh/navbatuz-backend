package uz.navbatuz.backend.provider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.provider.dto.BusinessHourResponse;
import uz.navbatuz.backend.provider.model.BusinessHour;

import java.util.List;
import java.util.UUID;

public interface BusinessHourRepository extends JpaRepository<BusinessHour, Long> {
    List<BusinessHour> findByProviderId(UUID providerId);

    void deleteByProviderId(UUID providerId);

    List<BusinessHourResponse> findByProvider_Id(UUID providerId);
}
