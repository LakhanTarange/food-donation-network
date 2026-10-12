package com.foodshare;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorIdOrderByCreatedAtDesc(Long donorId);
    List<Donation> findByStatusOrderByCreatedAtDesc(String status);

    List<Donation> findByAcceptedNgoIdOrderByCreatedAtDesc(Long ngoId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Donation d SET d.status = 'ACCEPTED', " +
           "d.acceptedNgoId = :ngoId, d.updatedAt = :now " +
           "WHERE d.id = :id AND d.status = 'AVAILABLE' " +
           "AND d.acceptedNgoId IS NULL")
    int acceptAvailableDonation(
        @Param("id") Long id,
        @Param("ngoId") Long ngoId,
        @Param("now") String now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Donation d SET d.status = 'COLLECTED', " +
           "d.updatedAt = :now WHERE d.id = :id " +
           "AND d.status = 'ACCEPTED' " +
           "AND d.acceptedNgoId = :ngoId")
    int completePickup(
        @Param("id") Long id,
        @Param("ngoId") Long ngoId,
        @Param("now") String now
    );

}
