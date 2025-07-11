package uz.navbatuz.backend.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.model.ServiceEntity;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByProviderIdAndIsActiveTrue(UUID providerId);


    List<ServiceEntity> findByWorkerIdAndIsActiveTrue(UUID workerId);

    List<ServiceEntity> findByProviderId(UUID providerId);

    List<ServiceEntity> findByWorkerId(UUID workerId);

    Page<ServiceEntity> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    Page<ServiceEntity> findByIsActiveTrue(Pageable pageable);


    @Query("""
    SELECT s FROM ServiceEntity s
    WHERE s.isActive = true
    AND (:category IS NULL OR s.category = :category)
    AND (:minPrice IS NULL OR s.price >= :minPrice)
    AND (:maxPrice IS NULL OR s.price <= :maxPrice)
    """)
    Page<ServiceEntity> searchByFilters(
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );



}
