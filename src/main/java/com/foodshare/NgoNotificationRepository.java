package com.foodshare;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NgoNotificationRepository
        extends JpaRepository<NgoNotification, Long> {

    List<NgoNotification> findByNgoIdOrderByIdDesc(Long ngoId);
}
