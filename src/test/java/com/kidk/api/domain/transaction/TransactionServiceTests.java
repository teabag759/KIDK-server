package com.kidk.api.domain.transaction;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.repository.AccountRepository;
import com.kidk.api.domain.transaction.entity.Transaction;
import com.kidk.api.domain.transaction.repository.TransactionRepository;
import com.kidk.api.domain.transaction.service.TransactionService;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TransactionServiceTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;

    /// 유저 생성
    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .firebaseUid("test-uid")
                .email("test@test.com")
                .name("테스트유저")
                .userType("PARENT")
                .build()
        );

    }

    @Test
    @DisplayName("입금 거래 생성 - 잔액 증가 확인")
    void depositSuccess() {
        // 1. 계좌 생성
        Account account = createAccount("테스트유저", BigDecimal.ZERO);

        // 2. 10000원 입금
        transactionService.createTransaction(
                account.getId(),
                "DEPOSIT",
                new BigDecimal("10000"),
                "SALARY",
                "월급",
                null
        );

        // 3. 잔액 확인
        Account updatedAccount = accountRepository.findById(account.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("출금 거래 생성 - 잔액 감소 확인")
    void withdrawalSuccess() {
        // 1. 계좌 생성
        Account account = createAccount("생활비", new BigDecimal("10000"));

        // 2. 3000원 사용
        transactionService.createTransaction(
                account.getId(),
                "WITHDRAWAL",
                new BigDecimal("3000"),
                "FOOD",
                "점심",
                null
        );

        // 3. 잔액 확인
        Account updatedAccount = accountRepository.findById(account.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("7000");
    }

    @Test
    @DisplayName("출금 실패 - 잔액 부족 시 예외 발생")
    void withdrawalFail_InsufficientBalance() {
        // 1. 계좌 생성 (초기 잔액 5,000원)
        Account account = createAccount("비상금", new BigDecimal("5000"));

        // 2. 10,000원 출금 시도 -> 예외 발생해야 함
        assertThatThrownBy(() ->
                transactionService.createTransaction(
                        account.getId(), "WITHDRAWAL", new BigDecimal("10000"), "SHOPPING", "무리한쇼핑", null
                )
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    @DisplayName("계좌 이체 성공 - 보내는 사람 감소, 받는 사람 증가")
    void transferSuccess() {
        // 1. 두 개의 계좌 준비
        Account fromAccount = createAccount("아빠지갑", new BigDecimal("50000"));
        Account toAccount = createAccount("아들지갑", new BigDecimal("0"));

        // 2. 이체 실행 (5,000원)
        transactionService.transfer(fromAccount.getId(), toAccount.getId(), new BigDecimal("5000"), "용돈");

        // 3. 잔액 검증
        Account updatedFrom = accountRepository.findById(fromAccount.getId()).get();
        Account updatedTo = accountRepository.findById(toAccount.getId()).get();

        assertThat(updatedFrom.getBalance()).isEqualByComparingTo("45000"); // 50000 - 5000
        assertThat(updatedTo.getBalance()).isEqualByComparingTo("5000");    // 0 + 5000

        // 4. 거래 내역이 2개(출금1, 입금1) 생겼는지 확인
        List<Transaction> fromHistory = transactionRepository.findByAccountIdOrderByCreatedAtDesc(fromAccount.getId());
        List<Transaction> toHistory = transactionRepository.findByAccountIdOrderByCreatedAtDesc(toAccount.getId());

        assertThat(fromHistory).hasSize(1);
        assertThat(fromHistory.get(0).getTransactionType()).isEqualTo("WITHDRAWAL"); // 보내는 쪽은 출금 기록

        assertThat(toHistory).hasSize(1);
        assertThat(toHistory.get(0).getTransactionType()).isEqualTo("DEPOSIT");      // 받는 쪽은 입금 기록
    }

    @Test
    @DisplayName("계좌 이체 실패 - 본인 계좌로 이체 시도")
    void transferFail_SameAccount() {
        Account account = createAccount("내계좌", new BigDecimal("10000"));

        assertThatThrownBy(() ->
                transactionService.transfer(account.getId(), account.getId(), new BigDecimal("1000"), "셀프이체")
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAME_ACCOUNT_TRANSFER);
    }


    // 헬퍼 메서드
    private Account createAccount(String name, BigDecimal balance) {
        return accountRepository.save(Account.builder()
                .user(testUser)
                .accountType("CHECKING")
                .accountName(name)
                .balance(balance)
                .active(true)
                .primary(true)
                .build());
    }

}
