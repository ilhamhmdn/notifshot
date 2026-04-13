package com.ilham.notifshot.repository;

import com.ilham.notifshot.domain.campaign.Campaign;
import com.ilham.notifshot.domain.campaign.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    Page<Campaign> findByTenantId(UUID tenantId, Pageable pageable);

    List<Campaign> findByStatus(CampaignStatus status);

    @Modifying
    @Query("UPDATE Campaign c SET c.status = :status WHERE c.id = :id")
    void updateStatus(UUID id, CampaignStatus status);

    @Modifying
    @Query("UPDATE Campaign c SET c.sentCount = c.sentCount + 1 WHERE c.id = :id")
    void incrementSentCount(UUID id);

    @Modifying
    @Query("UPDATE Campaign c SET c.failedCount = c.failedCount + 1 WHERE c.id = :id")
    void incrementFailedCount(UUID id);

    @Modifying
    @Query("UPDATE Campaign c SET c.skippedCount = c.skippedCount + 1 WHERE c.id = :id")
    void incrementSkippedCount(UUID id);

    @Modifying
    @Query("UPDATE Campaign c SET c.failedCount = c.failedCount - 1 WHERE c.id = :id")
    void decrementFailedCount(UUID id);
}