package com.kidk.api.domain.transaction.repository;

import com.kidk.api.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Transaction> {

    // 특정 계좌의 전체 거래 내역
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    // 카테고리별 거래 내역
    List<Transaction> findByAccountIdAndCategory(Long accountId, String category);

    // 페이지네이션 지원
    org.springframework.data.domain.Page<Transaction> findByAccountId(Long accountId,
            org.springframework.data.domain.Pageable pageable);
}
