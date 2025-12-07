package com.kidk.api.domain.account.dto;

import com.kidk.api.domain.account.entity.Account;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
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
}
