package com.kidk.api.domain.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TransferRequest {
    @NotNull(message = "출금할 계좌 ID는 필수입니다.")
    private Long fromAccountId;

    @NotNull(message = "입금받을 계좌 ID는 필수입니다.")
    private Long toAccountId;

    @NotNull(message = "이체 금액은 필수입니다.")
    @Positive(message = "이체 금액은 0보다 커야 합니다.")
    private BigDecimal amount;

    private String description;
}