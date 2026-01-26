package com.kidk.api.domain.transaction.service;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.repository.AccountRepository;
import com.kidk.api.domain.transaction.entity.Transaction;
import com.kidk.api.domain.transaction.repository.TransactionRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // 특정 계좌의 전체 거래 내역
    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    // 거래 생성 (동시성 제어 적용)
    @Transactional
    public Transaction createTransaction(
            Long accountId,
            String type,
            BigDecimal amount,
            String category,
            String description,
            Long relatedMissionId) {
        // 비관적 락으로 계좌 조회 (동시성 제어)
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 잔액 검증 (출금 시)
        if ("WITHDRAWAL".equals(type) && account.getBalance().compareTo(amount) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 잔액 계산
        BigDecimal newBalance;
        if ("DEPOSIT".equals(type) || "REWARD".equals(type)) {
            newBalance = account.getBalance().add(amount);
        } else {
            newBalance = account.getBalance().subtract(amount);
        }

        // Account 업데이트
        account.setBalance(newBalance);

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

    // 계좌 이체 (Transfer) - 동시성 제어 적용
    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        // 본인 계좌 이체 방지
        if (fromAccountId.equals(toAccountId)) {
            throw new CustomException(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }

        // 계좌 조회 (비관적 락 적용 - 데드락 방지를 위해 ID 순서대로 락 획득)
        Account fromAccount, toAccount;
        if (fromAccountId < toAccountId) {
            fromAccount = accountRepository.findByIdWithLock(fromAccountId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
            toAccount = accountRepository.findByIdWithLock(toAccountId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        } else {
            toAccount = accountRepository.findByIdWithLock(toAccountId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
            fromAccount = accountRepository.findByIdWithLock(fromAccountId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        }

        // 잔액 확인
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 출금 처리 (보내는 사람)
        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(amount);
        fromAccount.setBalance(fromNewBalance);
        accountRepository.save(fromAccount);

        Transaction withdrawTx = Transaction.builder()
                .account(fromAccount)
                .transactionType("WITHDRAWAL")
                .amount(amount)
                .balanceAfter(fromNewBalance)
                .category("TRANSFER")
                .description("이체 보냄: " + toAccount.getAccountName()) // 수신자 이름/별칭
                .build();
        transactionRepository.save(withdrawTx);

        // 입금 처리 (받는 사람)
        BigDecimal toNewBalance = toAccount.getBalance().add(amount);
        toAccount.setBalance(toNewBalance);
        accountRepository.save(toAccount);

        Transaction depositTx = Transaction.builder()
                .account(toAccount)
                .transactionType("DEPOSIT")
                .amount(amount)
                .balanceAfter(toNewBalance)
                .category("TRANSFER")
                .description("이체 받음: " + fromAccount.getAccountName()) // 송금자 이름/별칭
                .build();
        transactionRepository.save(depositTx);
    }
}
