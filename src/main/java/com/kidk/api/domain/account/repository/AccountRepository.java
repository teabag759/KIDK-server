package com.kidk.api.domain.account.repository;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // 1. 특정 유저의 모든 계좌 조회
    List<Account> findByUser(User user);

    List<Account> findByUserId(Long userId);

    // 2. 특정 유저의 활성 계좌 조회
    List<Account> findByUserIdAndActive(Long userId, boolean active);

    // 3. 특정 유저의 PRIMARY 계좌 조회 (null 허용이라 Boolean 사용)
    Optional<Account> findByUserIdAndPrimary(Long userId, Boolean primary);

    // 4. 계좌명 중복 확인 (유저별)
    boolean existsByUserIdAndAccountName(Long userId, String accountName);

    // 5. 계좌 단건 조회 (유저 + 계좌 id 조합)
    Optional<Account> findByIdAndUserId(Long id, Long userId);

    // 6. 계좌 타입 기준 조회 (ex. "SAVINGS", "CHECKING")
    List<Account> findByUserIdAndAccountType(Long userId, String accountType);

    // 7. 활성 + primary 조합 조회 (전형적 use-case)
    Optional<Account> findByUserIdAndActiveAndPrimary(Long userId, boolean active, Boolean primary);

    // 8. 비관적 락을 사용한 계좌 조회 (동시성 제어용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);
}
