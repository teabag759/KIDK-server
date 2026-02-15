package com.kidk.api.domain.transaction.repository;

import com.kidk.api.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionSpecification {

    // 계좌 ID로 필터링
    public static Specification<Transaction> hasAccountId(Long accountId) {
        return (root, query, criteriaBuilder) -> accountId == null ? null
                : criteriaBuilder.equal(root.get("account").get("id"), accountId);
    }

    // 거래 유형으로 필터링
    public static Specification<Transaction> hasTransactionType(String transactionType) {
        return (root, query, criteriaBuilder) -> transactionType == null ? null
                : criteriaBuilder.equal(root.get("transactionType"), transactionType);
    }

    // 카테고리로 필터링
    public static Specification<Transaction> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> category == null ? null
                : criteriaBuilder.equal(root.get("category"), category);
    }

    // 시작 날짜 이후 필터링
    public static Specification<Transaction> createdAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> startDate == null ? null
                : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    // 종료 날짜 이전 필터링
    public static Specification<Transaction> createdBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> endDate == null ? null
                : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }

    // 최소 금액 이상 필터링
    public static Specification<Transaction> amountGreaterThan(BigDecimal minAmount) {
        return (root, query, criteriaBuilder) -> minAmount == null ? null
                : criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    // 최대 금액 이하 필터링
    public static Specification<Transaction> amountLessThan(BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> maxAmount == null ? null
                : criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }
}
