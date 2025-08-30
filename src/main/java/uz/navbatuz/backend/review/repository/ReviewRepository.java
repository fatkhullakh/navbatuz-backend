package uz.navbatuz.backend.review.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.navbatuz.backend.review.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByAppointmentId(UUID appointmentId);

    @Query("""
           select r from Review r
           where r.provider.id = :providerId
             and r.deleted = false
             and r.publicVisible = true
           order by r.createdAt desc
           """)
    List<Review> listPublicByProvider(UUID providerId, Pageable pageable);

    @Query("""
           select r from Review r
           where r.worker.id = :workerId
             and r.deleted = false
             and r.publicVisible = true
           order by r.createdAt desc
           """)
    List<Review> listPublicByWorker(UUID workerId, Pageable pageable);

    @Query("""
           select coalesce(avg(r.rating), 0)
           from Review r
           where r.provider.id = :providerId and r.deleted = false
           """)
    Double avgByProviderId(UUID providerId);

    @Query("""
           select count(r)
           from Review r
           where r.provider.id = :providerId and r.deleted = false
           """)
    Long countByProviderId(UUID providerId);

    @Query("""
           select coalesce(avg(r.rating), 0)
           from Review r
           where r.worker.id = :workerId and r.deleted = false
           """)
    Double avgByWorkerId(UUID workerId);

    @Query("""
           select count(r)
           from Review r
           where r.worker.id = :workerId and r.deleted = false
           """)
    Long countByWorkerId(UUID workerId);

    Optional<Review> findFirstByAppointmentIdAndDeletedFalse(UUID appointmentId);
}
