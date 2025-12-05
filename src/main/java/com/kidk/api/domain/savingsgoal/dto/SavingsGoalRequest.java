package com.kidk.api.domain.savingsgoal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SavingsGoalRequest {
    @NotNull(message = "사용자 ID를 필수입니다")
    private Long userId;

    @NotNull(message = "목표 이름은 필수입니다")
    private String goalName;

    @NotNull(message = "목표 금액은 필수입니다")
    @Positive(message = "목표 금액은 양수여야 합니다")
    private BigDecimal targetAmount;

    private LocalDateTime targetDate;
}
