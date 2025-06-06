package com.example.utilitybilling.dto;

import com.example.utilitybilling.model.BillingFrequency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderUpdateRequest {
    @NotBlank
    private String name;

    @NotNull
    private BillingFrequency frequency;

    private String comment;

    @Min(1)
    @Max(28)
    private int dueDay;
}
