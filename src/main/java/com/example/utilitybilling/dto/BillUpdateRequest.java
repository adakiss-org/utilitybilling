package com.example.utilitybilling.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillUpdateRequest {
    @DecimalMin("0.00")
    private BigDecimal amount;

    @NotNull
    private com.example.utilitybilling.model.BillStatus status;
}
