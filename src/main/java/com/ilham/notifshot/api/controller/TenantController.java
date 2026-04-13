package com.ilham.notifshot.api.controller;

import com.ilham.notifshot.domain.tenant.Tenant;
import com.ilham.notifshot.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantRepository tenantRepository;

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantRepository.save(tenant));
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getTenants() {
        return ResponseEntity.ok(tenantRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenant(@PathVariable UUID id) {
        return tenantRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}