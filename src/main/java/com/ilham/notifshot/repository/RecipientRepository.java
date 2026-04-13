package com.ilham.notifshot.repository;

import com.ilham.notifshot.domain.recipient.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, UUID> {
    List<Recipient> findByCampaignId(UUID campaignId);
    long countByCampaignId(UUID campaignId);
}