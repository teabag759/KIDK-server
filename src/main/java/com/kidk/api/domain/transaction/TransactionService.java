package com.kidk.api.domain.transaction;

import com.kidk.api.domain.account.Account;
import com.kidk.api.domain.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // 특정 계좌의 전체 거래 내역
    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    // 거래 생성
    public Transaction createTransaction(
            Long accountId,
            String type,
            BigDecimal amount,
            String category,
            String description,
            Long relatedMissionId
    ) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // 잔액 계산
        BigDecimal newBalance =
                type.equals("DEPOSIT")
                        ? account.getBalance().add(amount)
                        : account.getBalance().subtract(amount);

        // Account 업데이트
        account.setBalance(newBalance);
        accountRepository.save(account);

        // 거래 생성
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(type)
                .amount(amount)
                .balanceAfter(newBalance)
                .category(category)
                .description(description)
                .relatedMissionId(relatedMissionId)
                .build();

        return transactionRepository.save(transaction);
    }


}
