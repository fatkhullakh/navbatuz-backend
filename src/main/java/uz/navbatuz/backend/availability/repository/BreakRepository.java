package uz.navbatuz.backend.availability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.navbatuz.backend.availability.dto.BreakResponse;
import uz.navbatuz.backend.availability.model.Break;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BreakRepository extends JpaRepository<Break, Integer> {
    void deleteByWorkerIdAndDateIn(UUID workerId, List<LocalDate> list);

    List<BreakResponse> findByWorkerIdAndDateBetween(UUID workerId, LocalDate from, LocalDate to);

    List<Break> findByWorkerIdAndDate(UUID workerId, LocalDate date);

    int deleteByWorkerIdAndDateAndStartTimeAndEndTime(
            UUID workerId, LocalDate date, LocalTime start, LocalTime end
    );

    Optional<Break> findByWorkerIdAndDateAndStartTimeAndEndTime(
            UUID workerId, LocalDate date, LocalTime start, LocalTime end
    );

    @Query("""
        select case when count(b) > 0 then true else false end
        from Break b
        where b.worker.id = :workerId
          and b.date = :date
          and (b.startTime < :end and b.endTime > :start)
    """)
    boolean existsOverlap(UUID workerId, LocalDate date, LocalTime start, LocalTime end);

    int deleteByIdAndWorkerId(Long breakId, UUID workerId);
}
