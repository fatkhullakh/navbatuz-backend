package uz.navbatuz.backend.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.navbatuz.backend.appointment.dto.AppointmentResponse;
import uz.navbatuz.backend.appointment.model.Appointment;
import uz.navbatuz.backend.common.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    boolean existsByWorkerIdAndDateAndStartTimeAndStatusIn(
            UUID workerId, LocalDate date, LocalTime startTime, Collection<AppointmentStatus> statuses);

    List<Appointment> findByWorkerIdAndDateAndStatusIn(
            UUID workerId, LocalDate date, Collection<AppointmentStatus> statuses);

    Optional<Appointment> findById(UUID appointmentId);

    List<Appointment> findByCustomerId(UUID customerId);

    List<Appointment> findByWorkerIdAndDateAfter(UUID workerId, LocalDate date);

    List<Appointment> findByWorkerIdAndDateAndStatusInOrderByStartTime(UUID workerId, LocalDate date, Set<AppointmentStatus> statuses);

    @Query(
            value = """
      SELECT * FROM appointment
      WHERE status = ANY(:statuses)
        AND end_at <= :now
      FOR UPDATE SKIP LOCKED
      LIMIT :limit
    """,
            nativeQuery = true
    )
    List<Appointment> pickOverdueForUpdate(
            @Param("statuses") String[] statuses,
            @Param("now") Instant now,
            @Param("limit") int limit
    );
}
