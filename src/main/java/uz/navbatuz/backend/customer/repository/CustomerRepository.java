package uz.navbatuz.backend.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.navbatuz.backend.customer.model.Customer;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query("SELECT c FROM Customer c WHERE c.user.email = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    boolean existsById(UUID id);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByUserId(UUID userId);


}
