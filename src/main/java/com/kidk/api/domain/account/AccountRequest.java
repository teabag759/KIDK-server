package com.kidk.api.domain.account;

import com.kidk.api.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@NoArgsConstructor
public class AccountRequest {
    private Long userId;
    private String accountType; // SPENDING, SAVINGS
    private String accountName;
    private BigDecimal initialBalance;
}
