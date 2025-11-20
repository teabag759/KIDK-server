package com.kidk.api.domain.account;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 유저의 모든 계좌 조회
     */
    @GetMapping("/user/{userId}")
    public List<Account> getUserAccounts(@PathVariable Long userId) {
        return accountService.getUserAccounts(userId);
    }

    /**
     * 유저의 활성 계좌 조회
     */
    @GetMapping("/user/{userId}/active")
    public List<Account> getActiveAccounts(@PathVariable Long userId) {
        return accountService.getActiveUserAccounts(userId);
    }

    /**
     * 유저의 primary 계좌 조회
     */
    @GetMapping("/user/{userId}/primary")
    public Account getPrimaryAccount(@PathVariable Long userId) {
        return accountService.getPrimaryAccount(userId);
    }

    /**
     * 단일 계좌 조회
     * 예: GET /api/accounts/3?userId=1
     */
    @GetMapping("/{accountId}")
    public Account getAccount(
            @PathVariable Long accountId,
            @RequestParam Long userId
    ) {
        return accountService.getAccountById(accountId, userId);
    }

    /**
     * 계좌 생성
     */
    @PostMapping
    public Account createAccount(
            @RequestParam Long userId,
            @RequestParam String accountType,
            @RequestParam String accountName,
            @RequestParam BigDecimal initialBalance
    ) {
        return accountService.createAccount(userId, accountType, accountName, initialBalance);
    }
}
