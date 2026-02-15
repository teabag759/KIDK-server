package com.kidk.api.domain.account.controller;

import com.kidk.api.domain.account.dto.AccountRequest;
import com.kidk.api.domain.account.dto.AccountResponse;
import com.kidk.api.domain.account.service.AccountService;
import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.transaction.service.TransactionService;
import com.kidk.api.domain.user.service.UserService;
import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserIdByFirebaseUid(userDetails.getUsername());
    }

    // 계좌 생성
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountRequest request) {
        Account account = accountService.createAccount(
                currentUserId(userDetails),
                request.getAccountType(),
                request.getAccountName(),
                request.getInitialBalance());
        return ResponseEntity.ok(ApiResponse.success(new AccountResponse(account)));
    }

    // 계좌 목록 조회 (본인)
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(userDetails);
        List<AccountResponse> accounts = accountService.getUserAccounts(userId).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    // 유저 계좌 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getUserAccounts(@PathVariable Long userId) {
        List<AccountResponse> accounts = accountService.getUserAccounts(userId).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    // 활성화 계좌 조회
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getActiveAccounts(@PathVariable Long userId) {
        List<AccountResponse> accounts = accountService.getActiveUserAccounts(userId).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    // 주 계좌 조회
    @GetMapping("/user/{userId}/primary")
    public ResponseEntity<ApiResponse<AccountResponse>> getPrimaryAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(new AccountResponse(accountService.getPrimaryAccount(userId))));
    }

    // 계좌 조회
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.success(
                new AccountResponse(accountService.getAccountById(accountId, currentUserId(userDetails)))));
    }

    // 계좌 잔액 조회
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getAccountBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId) {
        Account account = accountService.getAccountById(accountId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(account.getBalance()));
    }

    // 계좌 수정 (별칭)
    @PutMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountRequest.Update request) {
        AccountResponse response = new AccountResponse(
                accountService.updateAccount(accountId, currentUserId(userDetails), request.getAccountName()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 계좌 비활성화
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        accountService.deactivateAccount(accountId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 주 계좌 지정
    @PatchMapping("/{accountId}/primary")
    public ResponseEntity<ApiResponse<AccountResponse>> setPrimaryAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AccountResponse response = new AccountResponse(
                accountService.setPrimaryAccount(accountId, currentUserId(userDetails)));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 입금
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<ApiResponse<Void>> deposit(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        transactionService.createTransactionForUser(
                currentUserId(userDetails),
                accountId,
                "DEPOSIT",
                amount,
                "DEPOSIT",
                description != null ? description : "입금",
                null);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 출금
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        transactionService.createTransactionForUser(
                currentUserId(userDetails),
                accountId,
                "WITHDRAWAL",
                amount,
                "WITHDRAWAL",
                description != null ? description : "출금",
                null);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 계좌 이체
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody com.kidk.api.domain.transaction.dto.transfer.TransferRequest request) {
        transactionService.transferForUser(
                currentUserId(userDetails),
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 거래 내역 조회 (페이지네이션)
    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<ApiResponse<List<com.kidk.api.domain.transaction.dto.TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Page<com.kidk.api.domain.transaction.entity.Transaction> txPage =
                transactionService.getTransactionsPageForUser(currentUserId(userDetails), accountId, page, size);

        org.springframework.data.domain.Page<com.kidk.api.domain.transaction.dto.TransactionResponse> responsePage =
                txPage.map(com.kidk.api.domain.transaction.dto.TransactionResponse::new);

        return ResponseEntity.ok(ApiResponse.successPage(responsePage));
    }

    // 카테고리별 소비 통계
    @GetMapping("/{accountId}/statistics")
    public ResponseEntity<ApiResponse<java.util.Map<String, BigDecimal>>> getCategoryStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        java.time.LocalDateTime start = startDate != null ? java.time.LocalDateTime.parse(startDate) : null;
        java.time.LocalDateTime end = endDate != null ? java.time.LocalDateTime.parse(endDate) : null;
        java.util.Map<String, BigDecimal> stats = transactionService.getCategoryStatisticsForUser(
                currentUserId(userDetails), accountId, start, end);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
