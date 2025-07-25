package uz.navbatuz.backend.availability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.availability.dto.PlannedAvailabilityRequest;
import uz.navbatuz.backend.availability.dto.PlannedAvailabilityResponse;
import uz.navbatuz.backend.availability.model.PlannedAvailability;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface PlannedAvailabilityRepository extends JpaRepository<PlannedAvailability, Long> {

    void deleteByWorkerId(UUID workerId);

    List<PlannedAvailabilityResponse> getPlannedAvailabilitiesByWorkerId(UUID workerId);

    List<PlannedAvailabilityResponse> getByWorkerId(UUID workerId);

    List<PlannedAvailability> findByWorkerId(UUID workerId);

    PlannedAvailability findByWorkerIdAndDay(UUID workerId, DayOfWeek day);
}
