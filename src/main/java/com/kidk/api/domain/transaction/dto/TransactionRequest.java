package com.kidk.api.domain.transaction.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TransactionRequest {

    @NotNull(message = "계좌 ID는 필수입니다")
    private Long accountId;

    @NotBlank(message = "거래 유형은 필수입니다")
    @Pattern(regexp = "DEPOSIT|WITHDRAWAL|REWARD|TRANSFER", message = "유효하지 않은 거래 유형입니다")
    private String type; // DEPOSIT, WITHDRAWAL, REWARD, TRANSFER

    @NotNull(message = "금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "금액은 0보다 커야 합니다")
    @DecimalMax(value = "999999999.99", message = "금액이 너무 큽니다")
    private BigDecimal amount;

    @Size(max = 50, message = "카테고리는 50자 이하여야 합니다")
    private String category;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;

    private Long relatedMissionId;
}
