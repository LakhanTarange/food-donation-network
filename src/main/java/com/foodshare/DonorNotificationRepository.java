package com.foodshare;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorNotificationRepository
        extends JpaRepository<DonorNotification, Long> {

    List<DonorNotification> findByDonorIdOrderByIdDesc(Long donorId);

    long countByDonorIdAndIsReadFalse(Long donorId);

    java.util.Optional<DonorNotification> findByIdAndDonorId(
        Long id, Long donorId
    );

    List<DonorNotification> findByDonorIdAndIsReadFalse(Long donorId);

}
