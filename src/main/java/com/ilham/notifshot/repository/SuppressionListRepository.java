package com.ilham.notifshot.repository;

import com.ilham.notifshot.domain.notification.Channel;
import com.ilham.notifshot.domain.suppression.SuppressionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SuppressionListRepository extends JpaRepository<SuppressionList, UUID> {
    boolean existsByRecipientIdAndChannel(String recipientId, Channel channel);
}