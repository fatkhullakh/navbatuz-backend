package uz.navbatuz.backend.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.appointment.model.Appointment;
import uz.navbatuz.backend.appointment.repository.AppointmentRepository;
import uz.navbatuz.backend.common.AppointmentStatus;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.review.dto.CreateReviewRequest;
import uz.navbatuz.backend.review.dto.RatingSummary;
import uz.navbatuz.backend.review.dto.ReviewResponse;
import uz.navbatuz.backend.review.model.Review;
import uz.navbatuz.backend.review.repository.ReviewRepository;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProviderRepository providerRepository;
    private final WorkerRepository workerRepository;
    private final CurrentUserService currentUserService;

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final Duration REVIEW_WINDOW = Duration.ofDays(30);

    @Transactional
    public ReviewResponse create(CreateReviewRequest req) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        // rating bounds
        if (req.rating() == null || req.rating() < MIN_RATING || req.rating() > MAX_RATING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be 1..5");
        }

        // load appointment
        Appointment appt = appointmentRepository.findById(req.appointmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        // ownership — only the booking user
        User bookingUser = (appt.getCustomer() == null) ? null : appt.getCustomer().getUser();
        if (bookingUser == null || !bookingUser.getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only review your own appointment");
        }

        // status check
        AppointmentStatus st = appt.getStatus();
        boolean isCompleted = (st == AppointmentStatus.COMPLETED) || "FINISHED".equalsIgnoreCase(st.name());
        if (!isCompleted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed appointments can be reviewed");
        }

        // optional review window using (date + endTime)
        if (appt.getDate() != null && appt.getEndTime() != null) {
            ZonedDateTime apptEnd = ZonedDateTime.of(appt.getDate(), appt.getEndTime(), ZoneId.systemDefault());
            if (ZonedDateTime.now().isAfter(apptEnd.plus(REVIEW_WINDOW))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review window expired");
            }
        }

        // one review per appointment
        if (reviewRepository.existsByAppointmentId(appt.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this appointment");
        }

        // resolve worker & provider
        Worker worker = appt.getWorker();
        Provider provider = null;
        if (worker != null && worker.getProvider() != null) {
            provider = worker.getProvider();
        } else if (appt.getService() != null && appt.getService().getProvider() != null) {
            provider = appt.getService().getProvider();
        }

        // build & save
        Review r = Review.builder()
                .appointmentId(appt.getId())
                .provider(provider)
                .worker(worker)
                .author(bookingUser)
                .rating(req.rating())
                .comment(req.comment() == null ? null : req.comment().trim())
                .createdAt(ZonedDateTime.now())
                .deleted(false)
                .publicVisible(true)
                .build();

        r = reviewRepository.save(r);

        // aggregates
        if (provider != null) {
            Double avg = reviewRepository.avgByProviderId(provider.getId());
            Long cnt   = reviewRepository.countByProviderId(provider.getId());
            provider.setAvgRating(avg == null ? 0.0f : avg.floatValue());
            provider.setReviewsCount(cnt == null ? 0L : cnt);
            providerRepository.save(provider);
        }
        if (worker != null) {
            Double avg = reviewRepository.avgByWorkerId(worker.getId());
            Long cnt   = reviewRepository.countByWorkerId(worker.getId());
            worker.setAvgRating(avg == null ? 0.0f : avg.floatValue());
            worker.setReviewsCount(cnt == null ? 0L : cnt);
            workerRepository.save(worker);
        }

        // return DTO (avoid serializing entity graph)
        return map(r);
    }

    @Transactional
    public void delete(UUID reviewId) {
        var r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        var actorId = currentUserService.getCurrentUserId();
        boolean isAuthor = r.getAuthor() != null && r.getAuthor().getId().equals(actorId);
        boolean isAdmin  = currentUserService.getCurrentUserRole().name().equals("ADMIN");
        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this review");
        }

        r.setDeleted(true);
        reviewRepository.save(r);

        if (r.getProvider() != null) recalcProviderAggregate(r.getProvider().getId());
        if (r.getWorker()   != null) recalcWorkerAggregate(r.getWorker().getId());
    }

    public List<ReviewResponse> listByProvider(UUID providerId, int page, int size) {
        return reviewRepository.listPublicByProvider(providerId, PageRequest.of(page, size))
                .stream().map(this::map).toList();
    }

    public List<ReviewResponse> listByWorker(UUID workerId, int page, int size) {
        return reviewRepository.listPublicByWorker(workerId, PageRequest.of(page, size))
                .stream().map(this::map).toList();
    }

    public RatingSummary getProviderSummary(UUID providerId) {
        Double avg = reviewRepository.avgByProviderId(providerId);
        Long cnt   = reviewRepository.countByProviderId(providerId);
        return new RatingSummary(avg, cnt == null ? 0L : cnt);
    }

    public RatingSummary getWorkerSummary(UUID workerId) {
        Double avg = reviewRepository.avgByWorkerId(workerId);
        Long cnt   = reviewRepository.countByWorkerId(workerId);
        return new RatingSummary(avg, cnt == null ? 0L : cnt);
    }

    // ---- helpers ----

    private void recalcProviderAggregate(UUID providerId) {
        Double avg = reviewRepository.avgByProviderId(providerId);
        Long cnt   = reviewRepository.countByProviderId(providerId);

        var p = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
        p.setAvgRating(avg == null ? 0.0f : avg.floatValue());
        p.setReviewsCount(cnt == null ? 0L : cnt);
        providerRepository.save(p);
    }

    private void recalcWorkerAggregate(UUID workerId) {
        Double avg = reviewRepository.avgByWorkerId(workerId);
        Long cnt   = reviewRepository.countByWorkerId(workerId);

        var w = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
        w.setAvgRating(avg == null ? 0.0f : avg.floatValue());
        w.setReviewsCount(cnt == null ? 0L : cnt);
        workerRepository.save(w);
    }

    private ReviewResponse map(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getAppointmentId(),
                r.getProvider() == null ? null : r.getProvider().getId(),
                r.getWorker()   == null ? null : r.getWorker().getId(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt().toInstant(),
                r.getAuthor() == null ? "—" : r.getAuthor().getName()
        );
    }

    public ReviewResponse findByAppointment(UUID appointmentId) {
        return reviewRepository.findFirstByAppointmentIdAndDeletedFalse(appointmentId)
                .map(this::map)
                .orElse(null);
    }
}
