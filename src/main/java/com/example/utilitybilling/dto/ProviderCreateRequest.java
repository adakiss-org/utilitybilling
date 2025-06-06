package com.example.utilitybilling.dto;

import com.example.utilitybilling.model.BillingFrequency;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private BillingFrequency frequency;

    private String comment;

    @Min(1)
    @Max(28)
    private int dueDay;
}