package com.kidk.api.domain.account.dto;

import com.kidk.api.domain.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@AllArgsConstructor
@Builder
public class AccountResponse {
    private final Long id;
    private final Long userId;
    private final String accountType;
    private final String accountName;
    private final BigDecimal balance;
    private final boolean active;
    private final boolean primary;


    public AccountResponse(Account account) {
        this.id = account.getId();
        this.userId = account.getUser().getId();
        this.accountType = account.getAccountType();
        this.accountName = account.getAccountName();
        this.balance = account.getBalance();
        this.active = account.isActive();
        this.primary = account.getPrimary() != null && account.getPrimary();
    }

    // AccountResponse.java 내부
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                // ... 필요한 필드들 매핑
                .build();
    }
}
