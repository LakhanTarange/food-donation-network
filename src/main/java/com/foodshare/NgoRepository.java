package com.foodshare;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NgoRepository extends JpaRepository<Ngo, Long> {

    Optional<Ngo> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRegistrationNumber(String registrationNumber);
}
