package uz.navbatuz.backend.worker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.worker.model.Worker;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    @Query("SELECT c FROM Worker c WHERE c.user.email = :email")
    Optional<Worker> findByEmail(@Param("email") String email);

    List<Worker> findByProviderIdAndIsActiveTrue(UUID providerId);

    List<Worker> findByProviderId(UUID providerId);
}
