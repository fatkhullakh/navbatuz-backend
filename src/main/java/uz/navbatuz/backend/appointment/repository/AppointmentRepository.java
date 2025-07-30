package uz.navbatuz.backend.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.appointment.dto.AppointmentResponse;
import uz.navbatuz.backend.appointment.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByWorkerIdAndDate(UUID workerId, LocalDate date);

    boolean existsByWorkerIdAndDateAndStartTime(UUID workerId, LocalDate date, LocalTime startTime);

    Optional<Appointment> findById(UUID appointmentId);

    List<Appointment> findByCustomerId(UUID customerId);

    List<Appointment> findByWorkerIdAndDateAfter(UUID workerId, LocalDate date);
}
