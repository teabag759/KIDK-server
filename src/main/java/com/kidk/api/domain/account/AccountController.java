package com.kidk.api.domain.account;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 계좌 생성
    @PostMapping
    public AccountResponse createAccount(@RequestBody AccountRequest request) {
        Account account = accountService.createAccount(
                request.getUserId(),
                request.getAccountType(),
                request.getAccountName(),
                request.getInitialBalance()
        );
        return new AccountResponse(account);
    }

    // 유저 계좌 조회
    @GetMapping("/user/{userId}")
    public List<AccountResponse> getUserAccounts(@PathVariable Long userId) {
        return accountService.getUserAccounts(userId).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    // 활성화 계좌 조회
    @GetMapping("/user/{userId}/active")
    public List<AccountResponse> getActiveAccounts(@PathVariable Long userId) {
        return accountService.getActiveUserAccounts(userId).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    // 주 계좌 조회
    @GetMapping("/user/{userId}/primary")
    public AccountResponse getPrimaryAccount(@PathVariable Long userId) {
        return new AccountResponse(accountService.getPrimaryAccount(userId));
    }

    // 계좌 조회
    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable Long accountId, @RequestParam Long userId) {
        return new AccountResponse(accountService.getAccountById(accountId, userId));
    }
}
