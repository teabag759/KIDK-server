package com.kidk.api.domain.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TransactionRequest {
    private Long accountId;
    private String type;    // DEPOSIT, WITHDRAWAL, REWARD
    private BigDecimal amount;
    private String category;
    private String description;
    private Long relatedMissionId;
}
