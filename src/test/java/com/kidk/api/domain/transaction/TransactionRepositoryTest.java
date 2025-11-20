package com.kidk.api.domain.transaction;

import com.kidk.api.domain.account.Account;
import com.kidk.api.domain.account.AccountRepository;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("특정 계좌의 거래내역 전체 조회")
    void findByAccountId() {
        // 1. 유저 생성
        User user = userRepository.save(
                User.builder()
                        .firebaseUid("tx-test-uid")
                        .email("tx-test@example.com")
                        .userType("PARENT")
                        .name("거래테스트유저")
                        .status("ACTIVE")
                        .build()
        );

        // 2. 계좌 생성
        Account account = accountRepository.save(
                Account.builder()
                        .user(user)
                        .accountType("CHECKING")
                        .accountName("테스트계좌")
                        .balance(new BigDecimal("10000"))
                        .active(true)
                        .primary(true)
                        .build()
        );

        // 3. 거래내역 2건 생성
        Transaction tx1 = transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .transactionType("DEPOSIT")
                        .amount(new BigDecimal("3000"))
                        .balanceAfter(new BigDecimal("13000"))
                        .category("용돈")
                        .description("부모 용돈 지급")
                        .relatedMissionId(null)
                        .build()
        );

        Transaction tx2 = transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .transactionType("WITHDRAWAL")
                        .amount(new BigDecimal("500"))
                        .balanceAfter(new BigDecimal("12500"))
                        .category("간식")
                        .description("편의점 과자")
                        .relatedMissionId(null)
                        .build()
        );

        // 4. 조회
        List<Transaction> result =
                transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());

        assertThat(result).hasSize(2);

        // 정렬 확인 (DESC 정렬이므로 최근 거래가 먼저 나와야 함)
        assertThat(result.get(0).getTransactionType()).isEqualTo("WITHDRAWAL");
        assertThat(result.get(1).getTransactionType()).isEqualTo("DEPOSIT");
    }
}
