package com.kidk.api.domain.transaction.controller;

import com.kidk.api.domain.transaction.dto.TransactionRequest;
import com.kidk.api.domain.transaction.dto.TransactionResponse;
import com.kidk.api.domain.transaction.service.TransactionService;
import com.kidk.api.domain.transaction.dto.transfer.TransferRequest;
import com.kidk.api.domain.transaction.entity.Transaction;
import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final com.kidk.api.domain.user.service.UserService userService;

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserIdByFirebaseUid(userDetails.getUsername());
    }

    /// 특정 계좌 거래 내역 조회
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId) {
        ApiResponse<List<TransactionResponse>> responses = ApiResponse.success(transactionService
                .getTransactionsForUser(currentUserId(userDetails), accountId)
                .stream()
                .map(TransactionResponse::new)
                .toList());
        return ResponseEntity.ok(responses);
    }

    /// 거래 생성
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.createTransactionForUser(
                currentUserId(userDetails),
                request.getAccountId(),
                request.getType(),
                request.getAmount(),
                request.getCategory(),
                request.getDescription(),
                request.getRelatedMissionId());

        ApiResponse<TransactionResponse> response = ApiResponse.success(new TransactionResponse(transaction));
        return ResponseEntity.ok(response);
    }

    /// 계좌 이체 API
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid TransferRequest request) {
        transactionService.transferForUser(
                currentUserId(userDetails),
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription());

        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 거래 내역 필터링 & 페이지네이션
    @GetMapping("/account/{accountId}/filtered")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getFilteredTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        java.time.LocalDateTime start = startDate != null ? java.time.LocalDateTime.parse(startDate) : null;
        java.time.LocalDateTime end = endDate != null ? java.time.LocalDateTime.parse(endDate) : null;

        org.springframework.data.domain.Page<Transaction> transactions = transactionService
                .getTransactionsWithFiltersForUser(currentUserId(userDetails), accountId, start, end, type, category,
                        page, size);

        org.springframework.data.domain.Page<TransactionResponse> response = transactions.map(TransactionResponse::new);
        return ResponseEntity.ok(ApiResponse.successPage(response));
    }

    /// 카테고리별 통계
    @GetMapping("/account/{accountId}/statistics")
    public ResponseEntity<ApiResponse<java.util.Map<String, java.math.BigDecimal>>> getCategoryStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        java.time.LocalDateTime start = startDate != null ? java.time.LocalDateTime.parse(startDate) : null;
        java.time.LocalDateTime end = endDate != null ? java.time.LocalDateTime.parse(endDate) : null;

        java.util.Map<String, java.math.BigDecimal> stats = transactionService.getCategoryStatisticsForUser(
                currentUserId(userDetails), accountId, start, end);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

}
