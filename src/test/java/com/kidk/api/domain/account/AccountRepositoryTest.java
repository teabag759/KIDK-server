package com.kidk.api.domain.account;

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
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("특정 유저의 계좌 전체 조회")
    void findByUserId() {
        // 1) 테스트용 유저 직접 생성
        User user = userRepository.save(
                User.builder()
                        .firebaseUid("test-uid")
                        .email("test@example.com")
                        .userType("PARENT")
                        .name("테스트유저")
                        .status("ACTIVE")
                        .build()
        );

        // 2) 계좌 두 개 생성
        accountRepository.save(
                Account.builder()
                        .user(user)
                        .accountType("CHECKING")
                        .accountName("주계좌")
                        .balance(new BigDecimal("1000.00"))
                        .active(true)
                        .primary(true)
                        .build()
        );

        accountRepository.save(
                Account.builder()
                        .user(user)
                        .accountType("SAVINGS")
                        .accountName("비상금통장")
                        .balance(new BigDecimal("500.00"))
                        .active(true)
                        .primary(false)
                        .build()
        );

        // 3) 실제 테스트
        List<Account> accounts = accountRepository.findByUserId(user.getId());

        assertThat(accounts).hasSize(2);
    }
}
