package com.ilham.notifshot.repository;

import com.ilham.notifshot.domain.notification.NotificationJob;
import com.ilham.notifshot.domain.notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationJobRepository extends JpaRepository<NotificationJob, UUID> {

    Optional<NotificationJob> findByIdempotencyKey(String idempotencyKey);

    List<NotificationJob> findByCampaignIdAndStatus(UUID campaignId, NotificationStatus status);

    long countByCampaignIdAndStatus(UUID campaignId, NotificationStatus status);

    @Modifying
    @Query("UPDATE NotificationJob j SET j.status = :status WHERE j.id = :id")
    void updateStatus(UUID id, NotificationStatus status);

    @Query("SELECT j FROM NotificationJob j WHERE j.status = 'FAILED' AND j.retryCount < j.maxRetries AND j.campaign.id = :campaignId")
    List<NotificationJob> findRetryableJobsByCampaignId(UUID campaignId);
}