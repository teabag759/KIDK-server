package com.kidk.api.scenario;

import com.kidk.api.domain.account.Account;
import com.kidk.api.domain.account.AccountRepository;
import com.kidk.api.domain.account.AccountService;
import com.kidk.api.domain.family.Family;
import com.kidk.api.domain.family.FamilyService;
import com.kidk.api.domain.mission.Mission;
import com.kidk.api.domain.mission.MissionRequest;
import com.kidk.api.domain.mission.MissionService;
import com.kidk.api.domain.missionverification.MissionVerification;
import com.kidk.api.domain.missionverification.MissionVerificationService;
import com.kidk.api.domain.transaction.Transaction;
import com.kidk.api.domain.transaction.TransactionService;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // 테스트 후 데이터 롤백
public class KidkScenarioTests {

    @Autowired private UserRepository userRepository;
    @Autowired private FamilyService familyService;
    @Autowired private AccountService accountService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionService transactionService;
    @Autowired private MissionService missionService;
    @Autowired private MissionVerificationService verificationService;

    @Test
    @DisplayName("시나리오: 가족 생성 -> 이체 -> 미션 수행 -> 보상 지급")
    void fullScenarioTest() {
        // ==========================================
        // 1. 사용자 준비 (부모 1명, 자녀 1명)
        // ==========================================
        User parent = userRepository.save(User.builder()
                .firebaseUid("parent_uid").email("parent@test.com").name("아빠").userType("PARENT").status("ACTIVE").build());

        User child = userRepository.save(User.builder()
                .firebaseUid("child_uid").email("child@test.com").name("아들").userType("CHILD").status("ACTIVE").build());

        System.out.println("1. 사용자 생성 완료");

        // ==========================================
        // 2. 가족 연결 (부모가 생성 -> 자녀가 가입)
        // ==========================================
        Family family = familyService.createFamily(parent.getId(), "행복한 우리집");
        String inviteCode = family.getInviteCode();

        familyService.joinFamily(child.getId(), inviteCode);

        System.out.println("2. 가족 연결 완료 (초대코드: " + inviteCode + ")");

        // ==========================================
        // 3. 계좌 개설 및 초기 자금 충전
        // ==========================================
        // 부모 계좌 생성
        Account parentAccount = accountService.createAccount(parent.getId(), "CHECKING", "부모월급통장", BigDecimal.ZERO);
        // 자녀 계좌 생성 (주 계좌 설정됨)
        Account childAccount = accountService.createAccount(child.getId(), "SPENDING", "자녀용돈통장", BigDecimal.ZERO);

        // 부모 계좌에 10만원 입금 (외부 입금 가정)
        transactionService.createTransaction(parentAccount.getId(), "DEPOSIT", new BigDecimal("100000"), "월급", "입금", null);

        // 확인
        assertThat(accountRepository.findById(parentAccount.getId()).get().getBalance()).isEqualByComparingTo("100000");
        System.out.println("3. 계좌 개설 및 부모 입금 완료");

        // ==========================================
        // 4. 용돈 이체 (부모 -> 자녀)
        // ==========================================
        transactionService.transfer(parentAccount.getId(), childAccount.getId(), new BigDecimal("5000"), "첫 용돈");

        // 확인: 부모 95,000원, 자녀 5,000원
        assertThat(accountRepository.findById(parentAccount.getId()).get().getBalance()).isEqualByComparingTo("95000");
        assertThat(accountRepository.findById(childAccount.getId()).get().getBalance()).isEqualByComparingTo("5000");

        System.out.println("4. 용돈 이체 성공");

        // ==========================================
        // 5. 미션 생성 및 수행 (보상: 1000원)
        // ==========================================
        BigDecimal rewardAmount = new BigDecimal("1000");
        MissionRequest missionRequest = MissionRequest.builder()
                .creatorId(parent.getId())
                .ownerId(child.getId())
                .missionType("CLEANING")
                .title("방 청소하기")
                .description("깨끗하게")
                .targetAmount(BigDecimal.ZERO)
                .rewardAmount(rewardAmount)
                .status("IN_PROGRESS")
                .targetDate(LocalDate.now().plusDays(3))
                .build();

        Mission mission = missionService.createMission(missionRequest);
        System.out.println("5. 미션 생성 완료: " + mission.getTitle());

        // ==========================================
        // 6. 미션 인증 (자녀 제출 -> 부모 승인)
        // ==========================================
        // 자녀: 인증 사진 제출
        MissionVerification verification = verificationService.submitVerification(mission.getId(), child.getId(), "PHOTO", "clean_room.jpg");
        assertThat(verification.getStatus()).isEqualTo("PENDING");

        // 부모: 인증 승인
        verificationService.approveVerification(verification.getId(), parent.getId());

        // 검증: 미션 상태가 COMPLETED로 바뀌었는지
        Mission updatedMission = missionService.getMissionsForOwner(child.getId()).get(0);
        assertThat(updatedMission.getStatus()).isEqualTo("COMPLETED");
        System.out.println("6. 미션 승인 및 완료 처리 확인");

        // ==========================================
        // 7. 최종 보상 지급 확인
        // ==========================================
        // 자녀 계좌 잔액: 기존 5,000원 + 보상 1,000원 = 6,000원이어야 함
        Account finalChildAccount = accountRepository.findById(childAccount.getId()).get();
        assertThat(finalChildAccount.getBalance()).isEqualByComparingTo("6000");

        // 거래 내역에 "REWARD" 타입이 찍혔는지 확인
        List<Transaction> childTransactions = transactionService.getTransactions(childAccount.getId());
        boolean hasRewardTx = childTransactions.stream()
                .anyMatch(tx -> "REWARD".equals(tx.getTransactionType()) && tx.getAmount().compareTo(new BigDecimal("1000")) == 0);

        assertThat(hasRewardTx).isTrue();
        System.out.println("7. 보상금 입금 확인 완료 (최종 잔액: " + finalChildAccount.getBalance() + ")");
    }
}