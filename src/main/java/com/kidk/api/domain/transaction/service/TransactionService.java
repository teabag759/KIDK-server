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

    // 특정 계좌의 전체 거래 내역 (사용자 검증)
    public List<Transaction> getTransactionsForUser(Long userId, Long accountId) {
        verifyAccountOwnership(userId, accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    // 특정 계좌의 거래 내역 페이지 조회 (사용자 검증)
    public org.springframework.data.domain.Page<Transaction> getTransactionsPageForUser(
            Long userId, Long accountId, int page, int size) {
        verifyAccountOwnership(userId, accountId);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        return transactionRepository.findByAccountId(accountId, pageable);
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

        return createTransactionWithAccount(account, type, amount, category, description, relatedMissionId);
    }

    // 거래 생성 (사용자 검증 + 동시성 제어)
    @Transactional
    public Transaction createTransactionForUser(
            Long userId,
            Long accountId,
            String type,
            BigDecimal amount,
            String category,
            String description,
            Long relatedMissionId) {
        Account account = accountRepository.findByIdAndUserIdWithLock(accountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        return createTransactionWithAccount(account, type, amount, category, description, relatedMissionId);
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

    // 계좌 이체 (본인 계좌 간) - 사용자 검증
    @Transactional
    public void transferForUser(Long userId, Long fromAccountId, Long toAccountId, BigDecimal amount,
            String description) {
        if (fromAccountId.equals(toAccountId)) {
            throw new CustomException(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }

        Account fromAccount = accountRepository.findByIdAndUserIdWithLock(fromAccountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account toAccount = accountRepository.findByIdAndUserIdWithLock(toAccountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        transfer(fromAccount.getId(), toAccount.getId(), amount, description);
    }

    // Specification 기반 고급 필터링
    public org.springframework.data.domain.Page<Transaction> getTransactionsWithFilters(
            Long accountId,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate,
            String type,
            String category,
            int page,
            int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("createdAt").descending());

        // Specification 조합
        org.springframework.data.jpa.domain.Specification<Transaction> spec = org.springframework.data.jpa.domain.Specification
                .where(
                        com.kidk.api.domain.transaction.repository.TransactionSpecification.hasAccountId(accountId))
                .and(com.kidk.api.domain.transaction.repository.TransactionSpecification.hasTransactionType(type))
                .and(com.kidk.api.domain.transaction.repository.TransactionSpecification.hasCategory(category))
                .and(com.kidk.api.domain.transaction.repository.TransactionSpecification.createdAfter(startDate))
                .and(com.kidk.api.domain.transaction.repository.TransactionSpecification.createdBefore(endDate));

        return transactionRepository.findAll(spec, pageable);
    }

    // Specification 기반 고급 필터링 (사용자 검증)
    public org.springframework.data.domain.Page<Transaction> getTransactionsWithFiltersForUser(
            Long userId,
            Long accountId,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate,
            String type,
            String category,
            int page,
            int size) {
        verifyAccountOwnership(userId, accountId);
        return getTransactionsWithFilters(accountId, startDate, endDate, type, category, page, size);
    }

    // 카테고리별 통계
    public java.util.Map<String, java.math.BigDecimal> getCategoryStatistics(Long accountId,
            java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);

        return transactions.stream()
                .filter(t -> "WITHDRAWAL".equals(t.getTransactionType()))
                .filter(t -> startDate == null || t.getCreatedAt().isAfter(startDate))
                .filter(t -> endDate == null || t.getCreatedAt().isBefore(endDate))
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "ETC",
                        java.util.stream.Collectors.reducing(
                                java.math.BigDecimal.ZERO,
                                Transaction::getAmount,
                                java.math.BigDecimal::add)));
    }

    private Transaction createTransactionWithAccount(
            Account account,
            String type,
            BigDecimal amount,
            String category,
            String description,
            Long relatedMissionId) {
        if (!account.isActive()) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if ("WITHDRAWAL".equals(type) && account.getBalance().compareTo(amount) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal newBalance;
        if ("DEPOSIT".equals(type) || "REWARD".equals(type)) {
            newBalance = account.getBalance().add(amount);
        } else {
            newBalance = account.getBalance().subtract(amount);
        }

        account.setBalance(newBalance);

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

    // 카테고리별 통계 (사용자 검증)
    public java.util.Map<String, java.math.BigDecimal> getCategoryStatisticsForUser(Long userId, Long accountId,
            java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        verifyAccountOwnership(userId, accountId);
        return getCategoryStatistics(accountId, startDate, endDate);
    }

    private void verifyAccountOwnership(Long userId, Long accountId) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCESS_DENIED));
    }
}
