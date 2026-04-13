package com.ilham.notifshot.domain.campaign;

import com.ilham.notifshot.domain.notification.Channel;
import com.ilham.notifshot.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;

    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private int totalRecipients;

    @Column(nullable = false)
    private int sentCount;

    @Column(nullable = false)
    private int failedCount;

    @Column(nullable = false)
    private int skippedCount;

    @Column(name = "is_transactional", nullable = false)
    private boolean transactional;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = CampaignStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public double getDeliveryRate() {
        if (totalRecipients == 0) return 0.0;
        return (double) sentCount / totalRecipients * 100;
    }
}