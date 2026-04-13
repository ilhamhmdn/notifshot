package com.ilham.notifshot.repository;

import com.ilham.notifshot.domain.notification.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
    List<DeliveryAttempt> findByNotificationJobId(UUID notificationJobId);
    int countByNotificationJobId(UUID notificationJobId);
}