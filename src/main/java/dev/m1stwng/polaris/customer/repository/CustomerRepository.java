package dev.m1stwng.polaris.customer.repository;

import dev.m1stwng.polaris.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByUserId(UUID userId);

    Optional<Customer> findByUserId(UUID userId);
}
