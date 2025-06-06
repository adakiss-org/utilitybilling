package com.example.utilitybilling.dto;

import com.example.utilitybilling.model.BillStatus;
import com.example.utilitybilling.model.BillingFrequency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderResponse {
    private UUID id;
    private String name;
    private BillingFrequency frequency;
    private String comment;
    private int dueDay;
    private LocalDateTime createdAt;
}