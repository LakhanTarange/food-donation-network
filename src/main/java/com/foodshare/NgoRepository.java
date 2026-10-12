package com.foodshare;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NgoRepository extends JpaRepository<Ngo, Long> {

    Optional<Ngo> findByEmail(String email);

    List<Ngo> findByVerifiedTrue();

    boolean existsByEmail(String email);

    boolean existsByRegistrationNumber(String registrationNumber);
}
