package uz.navbatuz.backend.availability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.availability.dto.ActualAvailabilityResponse;
import uz.navbatuz.backend.availability.model.ActualAvailability;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ActualAvailabilityRepository extends JpaRepository<ActualAvailability, Long> {

    void deleteByWorkerIdAndDateIn(UUID workerId, List<LocalDate> list);

    List<ActualAvailabilityResponse> getByWorkerId(UUID workerId);

    List<ActualAvailabilityResponse> findByWorkerIdAndDateBetween(UUID workerId, LocalDate from, LocalDate to);
}
