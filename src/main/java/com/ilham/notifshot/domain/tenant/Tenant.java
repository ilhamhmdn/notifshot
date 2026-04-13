package com.ilham.notifshot.domain.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private int monthlyCampaignLimit;

    @Column(nullable = false)
    private int monthlyMessageLimit;

    @Column(nullable = false)
    private int campaignsUsed;

    @Column(nullable = false)
    private int messagesUsed;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean hasExceededCampaignLimit() {
        return campaignsUsed >= monthlyCampaignLimit;
    }

    public boolean hasExceededMessageLimit(int recipientCount) {
        return (messagesUsed + recipientCount) > monthlyMessageLimit;
    }
}