package uz.navbatuz.backend.availability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.availability.dto.BreakResponse;
import uz.navbatuz.backend.availability.model.Break;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BreakRepository extends JpaRepository<Break, Integer> {
    void deleteByWorkerIdAndDateIn(UUID workerId, List<LocalDate> list);

    List<BreakResponse> findByWorkerIdAndDateBetween(UUID workerId, LocalDate from, LocalDate to);

    List<Break> findByWorkerIdAndDate(UUID workerId, LocalDate date);
}
