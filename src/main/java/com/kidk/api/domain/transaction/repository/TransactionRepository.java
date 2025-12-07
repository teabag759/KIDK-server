package com.kidk.api.domain.transaction.repository;

import com.kidk.api.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 특정 계좌의 전체 거래 내역
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    // 카테고리로 검색
    List<Transaction> findByAccountIdAndCategory(Long accountId, String category);
}
