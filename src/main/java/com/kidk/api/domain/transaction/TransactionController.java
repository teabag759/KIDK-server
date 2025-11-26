package com.kidk.api.domain.transaction;

import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // 특정 계좌 거래 내역 조회
    @GetMapping("/account/{accountId}")
    public ApiResponse<List<TransactionResponse>> getTransactions(@PathVariable Long accountId) {
        List<TransactionResponse> responses = transactionService.getTransactions(accountId).stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    // 거래 생성
    @PostMapping
    public ApiResponse<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(
                request.getAccountId(),
                request.getType(),
                request.getAmount(),
                request.getCategory(),
                request.getDescription(),
                request.getRelatedMissionId()
        );
        return ApiResponse.success(new TransactionResponse(transaction));
    }

    // 계좌 이체 API
    @PostMapping("/transfer")
    public ApiResponse<Void> transfer(@RequestBody @Valid TransferRequest request) {
        transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription()
        );
        return ApiResponse.success();
    }

}
