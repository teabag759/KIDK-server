package com.kidk.api.domain.account.service;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.repository.AccountRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
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

    // 주 계좌 조회
    public Account getPrimaryAccount(Long userId) {
        return accountRepository.findByUserIdAndPrimaryTrue(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    // 계좌 수정 (별칭)
    @Transactional
    public Account updateAccount(Long accountId, Long userId, String newAccountName) {
        Account account = getAccountById(accountId, userId);
        account.setAccountName(newAccountName);
        return accountRepository.save(account);
    }

    // 계좌 비활성화
    @Transactional
    public void deactivateAccount(Long accountId, Long userId) {
        Account account = getAccountById(accountId, userId);

        // 주 계좌는 비활성화 불가
        if (Boolean.TRUE.equals(account.getPrimary())) {
            throw new CustomException(ErrorCode.CANNOT_DEACTIVATE_PRIMARY_ACCOUNT);
        }

        account.setActive(false);
        accountRepository.save(account);
    }

    // 주 계좌 지정
    @Transactional
    public Account setPrimaryAccount(Long accountId, Long userId) {
        // 기존 주 계좌 해제
        accountRepository.findByUserIdAndPrimaryTrue(userId)
                .ifPresent(account -> {
                    account.setPrimary(false);
                    accountRepository.save(account);
                });

        // 새 주 계좌 지정
        Account account = getAccountById(accountId, userId);
        account.setPrimary(true);
        return accountRepository.save(account);
    }

    // 입금
    @Transactional
    public void deposit(Long accountId, Long userId, java.math.BigDecimal amount, String description) {
        Account account = getAccountById(accountId, userId);

        if (!account.isActive()) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    // 출금
    @Transactional
    public void withdraw(Long accountId, Long userId, java.math.BigDecimal amount, String description) {
        Account account = getAccountById(accountId, userId);

        if (!account.isActive()) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    // 단일 계좌 조회 - 보안: userId도 함께 체크
    public Account getAccountById(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    // 계좌 생성
    @Transactional
    public Account createAccount(Long userId, String accountType, String accountName, BigDecimal initBalance) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (accountRepository.existsByUserIdAndAccountName(userId, accountName)) {
            throw new CustomException(ErrorCode.DUPLICATE_ACCOUNT_NAME);
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
