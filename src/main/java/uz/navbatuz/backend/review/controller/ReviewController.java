package uz.navbatuz.backend.review.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.review.dto.CreateReviewRequest;
import uz.navbatuz.backend.review.dto.RatingSummary;
import uz.navbatuz.backend.review.dto.ReviewResponse;
import uz.navbatuz.backend.review.service.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ReviewResponse> create(@RequestBody CreateReviewRequest req) {
        var dto = reviewService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Public listing (for provider page)
    @GetMapping("/public/provider/{providerId}")
    public List<ReviewResponse> listProvider(
            @PathVariable UUID providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reviewService.listByProvider(providerId, page, size);
    }

    @GetMapping("/public/worker/{workerId}")
    public List<ReviewResponse> listWorker(
            @PathVariable UUID workerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reviewService.listByWorker(workerId, page, size);
    }

    @GetMapping("/public/provider/{providerId}/summary")
    public RatingSummary providerSummary(@PathVariable UUID providerId) {
        return reviewService.getProviderSummary(providerId);
    }

    @GetMapping("/public/worker/{workerId}/summary")
    public RatingSummary workerSummary(@PathVariable UUID workerId) {
        return reviewService.getWorkerSummary(workerId);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ReviewResponse> byAppointment(@PathVariable UUID appointmentId) {
        var dto = reviewService.findByAppointment(appointmentId);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }
}
