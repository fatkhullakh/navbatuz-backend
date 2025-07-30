package uz.navbatuz.backend.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.appointment.model.AppointmentStatusHistory;

import java.util.List;
import java.util.UUID;

public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, UUID> {
    List<AppointmentStatusHistory> findByAppointmentId(UUID appointmentId);
}
