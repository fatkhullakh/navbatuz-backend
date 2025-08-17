package uz.navbatuz.backend.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.navbatuz.backend.appointment.dto.AppointmentResponse;
import uz.navbatuz.backend.appointment.model.Appointment;
import uz.navbatuz.backend.common.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    boolean existsByWorkerIdAndDateAndStartTimeAndStatusIn(
            UUID workerId, LocalDate date, LocalTime startTime, Collection<AppointmentStatus> statuses);

    List<Appointment> findByWorkerIdAndDateAndStatusIn(
            UUID workerId, LocalDate date, Collection<AppointmentStatus> statuses);

    Optional<Appointment> findById(UUID appointmentId);

    List<Appointment> findByCustomerId(UUID customerId);

    List<Appointment> findByWorkerIdAndDateAfter(UUID workerId, LocalDate date);


//    @Query("""
//       select a
//       from Appointment a
//       where a.customer.id = :userId or a.worker.id = :userId
//       order by a.date asc, a.startTime asc
//    """)
//    List<Appointment> findAllByCustomerOrWorker(@Param("userId") UUID userId);
}
