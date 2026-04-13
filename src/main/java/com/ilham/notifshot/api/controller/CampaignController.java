package com.ilham.notifshot.api.controller;

import com.ilham.notifshot.api.dto.CampaignResponse;
import com.ilham.notifshot.api.dto.CreateCampaignRequest;
import com.ilham.notifshot.application.campaign.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createCampaign(
            @RequestPart("campaign") @Valid CreateCampaignRequest request,
            @RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CSV file is required"));
        }

        try {
            CampaignResponse response = campaignService.createCampaign(request, file);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create campaign: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create campaign"));
        }
    }

    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> getCampaigns(
            @RequestParam(required = false) UUID tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(campaignService.getCampaigns(tenantId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(campaignService.getCampaign(id));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/retry-failures")
    public ResponseEntity<Map<String, Object>> retryFailures(@PathVariable UUID id) {
        try {
            int retried = campaignService.retryFailures(id);
            return ResponseEntity.accepted()
                    .body(Map.of(
                            "campaignId", id,
                            "jobsQueued", retried,
                            "message", retried + " failed jobs re-queued for retry"
                    ));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}