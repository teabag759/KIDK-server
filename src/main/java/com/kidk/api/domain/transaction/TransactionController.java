package com.kidk.api.domain.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // 특정 계좌 거래 내역 조회
    @GetMapping("/account/{accountId}")
    public List<TransactionResponse> getTransactions(@PathVariable Long accountId) {
        return transactionService.getTransactions(accountId).stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    // 거래 생성
    @PostMapping
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {

        Transaction transaction = transactionService.createTransaction(
                request.getAccountId(),
                request.getType(),
                request.getAmount(),
                request.getCategory(),
                request.getDescription(),
                request.getRelatedMissionId()
        );

        return new TransactionResponse(transaction);

    }

}
