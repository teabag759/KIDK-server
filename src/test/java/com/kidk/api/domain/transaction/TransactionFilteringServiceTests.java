package com.kidk.api.domain.transaction;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.repository.AccountRepository;
import com.kidk.api.domain.transaction.entity.Transaction;
import com.kidk.api.domain.transaction.repository.TransactionRepository;
import com.kidk.api.domain.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 필터링 및 통계 테스트")
class TransactionFilteringServiceTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account testAccount;
    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .accountName("테스트 계좌")
                .balance(new BigDecimal("10000"))
                .build();

        transaction1 = Transaction.builder()
                .id(1L)
                .account(testAccount)
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("5000"))
                .category("FOOD")
                .balanceAfter(new BigDecimal("5000"))
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        transaction2 = Transaction.builder()
                .id(2L)
                .account(testAccount)
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("3000"))
                .category("TRANSPORT")
                .balanceAfter(new BigDecimal("2000"))
                .createdAt(LocalDateTime.of(2024, 1, 5, 14, 0))
                .build();

        transaction3 = Transaction.builder()
                .id(3L)
                .account(testAccount)
                .transactionType("DEPOSIT")
                .amount(new BigDecimal("10000"))
                .category(null)
                .balanceAfter(new BigDecimal("12000"))
                .createdAt(LocalDateTime.of(2024, 1, 10, 9, 0))
                .build();
    }

    @Test
    @DisplayName("페이지네이션 필터링 - Specification 사용")
    void getTransactionsWithFilters_WithSpecification() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 2);

        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(transactionPage);

        // When
        Page<Transaction> result = transactionService.getTransactionsWithFilters(
                1L,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 31, 23, 59),
                "WITHDRAWAL",
                null,
                0,
                10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(transactionRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("카테고리별 통계 계산")
    void getCategoryStatistics() {
        // Given
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(transaction1, transaction2, transaction3));

        // When
        Map<String, BigDecimal> statistics = transactionService.getCategoryStatistics(
                1L,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 31, 23, 59));

        // Then
        assertThat(statistics).containsKeys("FOOD", "TRANSPORT");
        assertThat(statistics.get("FOOD")).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(statistics.get("TRANSPORT")).isEqualByComparingTo(new BigDecimal("3000"));
        // DEPOSIT는 제외되어야 함
        assertThat(statistics).doesNotContainKey("ETC");
    }

    @Test
    @DisplayName("카테고리별 통계 - null 카테고리는 ETC로 처리")
    void getCategoryStatistics_NullCategoryAsETC() {
        // Given
        Transaction withdrawalWithoutCategory = Transaction.builder()
                .id(4L)
                .account(testAccount)
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("1000"))
                .category(null)
                .balanceAfter(new BigDecimal("1000"))
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(transaction1, transaction2, withdrawalWithoutCategory));

        // When
        Map<String, BigDecimal> statistics = transactionService.getCategoryStatistics(
                1L,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 31, 23, 59));

        // Then
        assertThat(statistics).containsKey("ETC");
        assertThat(statistics.get("ETC")).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("기간 필터링 - 시작일 이후만")
    void getCategoryStatistics_WithStartDateOnly() {
        // Given
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(transaction1, transaction2, transaction3));

        // When
        Map<String, BigDecimal> statistics = transactionService.getCategoryStatistics(
                1L,
                LocalDateTime.of(2024, 1, 3, 0, 0),
                null);

        // Then
        // transaction1은 1월 1일이므로 제외, transaction2만 포함
        assertThat(statistics).containsKey("TRANSPORT");
        assertThat(statistics.get("TRANSPORT")).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(statistics).doesNotContainKey("FOOD");
    }
}
