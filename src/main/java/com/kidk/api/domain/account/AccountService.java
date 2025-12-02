package com.kidk.api.domain.account;

import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
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
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // 특정 유저의 모든 계좌 조회
    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    // 특정 유저의 활성 계좌 조회
    public List<Account> getActiveUserAccounts(Long userId) {
        return accountRepository.findByUserIdAndActive(userId, true);
    }

    // 특정 유저의 primary 계좌 조회 (없을 수 있어서 Optional 처리)
    public Account getPrimaryAccount(Long userId) {
        return accountRepository
                .findByUserIdAndPrimary(userId, true)
                .orElseThrow(() -> new RuntimeException("Primary account not found"));
    }

    // 단일 계좌 조회 - 보안: userId도 함께 체크
    public Account getAccountById(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Account not found or access denied"));
    }

    // 계좌 생성
    @Transactional
    public Account createAccount(Long userId, String accountType, String accountName, BigDecimal initBalance) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (accountRepository.existsByUserIdAndAccountName(userId, accountName)) {
            throw new RuntimeException("Duplicated account name");
        }

        // 기존 계좌가 없을 경우, 이번에 만드는 계좌가 primary 계좌가 되도록
        boolean isFirstAccount = accountRepository.findByUserId(userId).isEmpty();

        Account account = Account.builder()
                .user(user)
                .accountType(accountType)
                .accountName(accountName)
                .balance(initBalance)
                .active(true)
                .primary(isFirstAccount)
                .build();

        return accountRepository.save(account);
    }
}
