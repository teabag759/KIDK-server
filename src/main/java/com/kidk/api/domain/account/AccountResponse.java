package com.kidk.api.domain.account;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
public class AccountResponse {
    private Long id;
    private Long userId;
    private String accountType;
    private String accountName;
    private BigDecimal balance;
    private boolean active;
    private boolean primary;


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
