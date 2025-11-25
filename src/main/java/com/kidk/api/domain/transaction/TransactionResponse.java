package com.kidk.api.domain.transaction;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
public class TransactionResponse {
    private final Long id;
    private final Long accountId;
    private final String type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String category;
    private final String description;
    private final LocalDateTime createdAt;

    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.accountId = transaction.getAccount().getId();
        this.type = transaction.getTransactionType();
        this.amount = transaction.getAmount();
        this.balanceAfter = transaction.getBalanceAfter();
        this.category = transaction.getCategory();
        this.description = transaction.getDescription();
        this.createdAt = transaction.getCreatedAt();
    }
}