package com.kidk.api.domain.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    // 특정 계좌 거래 내역 조회
    @GetMapping("/account/{accountId}")
    public List<Transaction> getTransactions(@PathVariable Long accountId) {
        return transactionService.getTransactions(accountId);
    }

    // 거래 생성
    @PostMapping
    public Transaction createTransaction(
            @RequestParam Long accountId,
            @RequestParam String type,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long relatedMissionId
    ) {
        return transactionService.createTransaction(
                accountId, type, amount, category, description, relatedMissionId
        );
    }

}
