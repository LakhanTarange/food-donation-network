package com.foodshare;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorNotificationRepository
        extends JpaRepository<DonorNotification, Long> {

    List<DonorNotification> findByDonorIdOrderByIdDesc(Long donorId);
}
