package com.kidk.api.domain.account.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@NoArgsConstructor
public class AccountRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotBlank(message = "계좌 유형은 필수입니다")
    @Pattern(regexp = "SPENDING|SAVINGS|CHECKING", message = "유효하지 않은 계좌 유형입니다")
    private String accountType; // SPENDING, SAVINGS, CHECKING

    @NotBlank(message = "계좌 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "계좌 이름은 1-100자 사이여야 합니다")
    private String accountName;

    @NotNull(message = "초기 잔액은 필수입니다")
    @DecimalMin(value = "0.00", message = "초기 잔액은 0 이상이어야 합니다")
    @DecimalMax(value = "999999999.99", message = "초기 잔액이 너무 큽니다")
    private BigDecimal initialBalance;

    // 계좌 수정용 DTO
    @Getter
    @NoArgsConstructor
    public static class Update {
        @NotBlank(message = "계좌 이름은 필수입니다")
        @Size(min = 1, max = 100, message = "계좌 이름은 1-100자 사이여야 합니다")
        private String accountName;
    }
}
