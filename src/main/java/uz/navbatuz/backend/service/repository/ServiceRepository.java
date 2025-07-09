package uz.navbatuz.backend.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.model.ServiceEntity;


import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByProviderIdAndIsActiveTrue(UUID providerId);

}
