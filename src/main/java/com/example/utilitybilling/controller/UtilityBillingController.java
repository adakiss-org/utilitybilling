package com.example.utilitybilling.controller;

import com.example.utilitybilling.dto.*;
import com.example.utilitybilling.model.*;
import com.example.utilitybilling.service.UtilityBillingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Utility Billing API", description = "Manage utility providers and bills")
public class UtilityBillingController {

    private final UtilityBillingService billingService;

    @Operation(summary = "Fetch all currently registered providers")
    @GetMapping("/providers")
    public List<ProviderResponse> getAllProviders() {
        return billingService.findAllProviders().stream().map(this::toProviderResponse).collect(Collectors.toList());
    }

    @Operation(summary = "Create a new provider")
    @PostMapping("/providers")
    public ResponseEntity<ProviderResponse> createProvider(@Valid @RequestBody ProviderCreateRequest request) {
        UtilityProvider p = UtilityProvider.builder()
                .name(request.getName())
                .frequency(request.getFrequency())
                .comment(request.getComment())
                .dueDay(request.getDueDay())
                .build();
        UtilityProvider created = billingService.createProvider(p);
        return new ResponseEntity<>(toProviderResponse(created), HttpStatus.CREATED);
    }

    @Operation(summary = "Update existing provider")
    @PutMapping("/providers/{providerId}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable UUID providerId,
            @Valid @RequestBody ProviderUpdateRequest request) {
        UtilityProvider p = UtilityProvider.builder()
                .id(providerId)
                .name(request.getName())
                .frequency(request.getFrequency())
                .comment(request.getComment())
                .dueDay(request.getDueDay())
                .build();
        UtilityProvider updated = billingService.updateProvider(providerId, p);
        return ResponseEntity.ok(toProviderResponse(updated));
    }

    @Operation(summary = "Update existing bill (amount and status)")
    @PatchMapping("/bills/{billId}")
    public ResponseEntity<BillResponse> updateBill(
            @PathVariable UUID billId,
            @Valid @RequestBody BillUpdateRequest request) {
        Bill updated = billingService.updateBill(billId, request.getAmount(), request.getStatus());
        return ResponseEntity.ok(toBillResponse(updated));
    }

    @Operation(summary = "Fetch bills due in a specific year-month (yyyy-MM)")
    @GetMapping("/bills")
    public List<BillResponse> getBillsByYearMonth(@RequestParam("yearMonth") String yearMonth) {
        return billingService.findBillsByYearMonth(yearMonth).stream().map(this::toBillResponse).collect(Collectors.toList());
    }

    // Mapping helpers

    private ProviderResponse toProviderResponse(UtilityProvider p) {
        return ProviderResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .frequency(p.getFrequency())
                .comment(p.getComment())
                .dueDay(p.getDueDay())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private BillResponse toBillResponse(Bill b) {
        return BillResponse.builder()
                .id(b.getId())
                .providerId(b.getProviderId())
                .amount(b.getAmount())
                .status(b.getStatus())
                .dueDate(b.getDueDate())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}