package com.example.utilitybilling.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utility_provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityProvider {

    @Id
    @Column(nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length=20)
    private BillingFrequency frequency;

    @Column(columnDefinition = "VARCHAR(1024)")
    private String comment;

    @Min(1)
    @Max(28) // To avoid issues with due day beyond end of month
    @Column(name = "due_day", nullable = false)
    private int dueDay;

    @Column(precision = 19, scale = 4)
    private BigDecimal defaultAmount;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
