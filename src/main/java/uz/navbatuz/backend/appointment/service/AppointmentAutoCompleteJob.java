package uz.navbatuz.backend.appointment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.appointment.model.Appointment;
import uz.navbatuz.backend.appointment.repository.AppointmentRepository;
import uz.navbatuz.backend.common.AppointmentStatus;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentAutoCompleteJob {

    private final AppointmentRepository repo;

    // which statuses are eligible for auto-complete
    private static final String[] ELIGIBLE = {"BOOKED", "CONFIRMED", "RESCHEDULED"};

    // run every 5 minutes (tweak in properties)
    @Scheduled(cron = "${appointments.autocomplete.cron:0 */5 * * * *}")
    public void run() {
        final int batchSize = 200; // tune to your traffic
        int total = 0;

        while (true) {
            int processed = processBatch(batchSize);
            total += processed;
            if (processed < batchSize) break; // nothing left to claim
        }

        if (total > 0) {
            log.info("Auto-completed {} overdue appointments", total);
        }
    }

    @Transactional
    int processBatch(int batchSize) {
        List<Appointment> batch = repo.pickOverdueForUpdate(ELIGIBLE, Instant.now(), batchSize);
        if (batch.isEmpty()) return 0;

        Instant now = Instant.now();
        for (Appointment a : batch) {
            a.setStatus(AppointmentStatus.valueOf("COMPLETED"));
            // publish event to trigger review email/push, if you have such flow
            // domainEvents.publish(new AppointmentCompletedEvent(a.getId()));
        }
        repo.saveAll(batch);
        return batch.size();
    }
}

