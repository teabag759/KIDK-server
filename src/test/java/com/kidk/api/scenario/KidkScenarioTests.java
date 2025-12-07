package com.kidk.api.scenario;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.repository.AccountRepository;
import com.kidk.api.domain.account.service.AccountService;
import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.family.service.FamilyService;
import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.dto.MissionRequest;
import com.kidk.api.domain.mission.service.MissionService;
import com.kidk.api.domain.missionverification.entity.MissionVerification;
import com.kidk.api.domain.missionverification.service.MissionVerificationService;
import com.kidk.api.domain.transaction.entity.Transaction;
import com.kidk.api.domain.transaction.service.TransactionService;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
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
@Transactional
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
                .firebaseUid("parent_uid")
                .email("parent@test.com")
                .name("아빠")
                .userType("PARENT")
                .status("ACTIVE")
                .build());

        User child = userRepository.save(User.builder()
                .firebaseUid("child_uid")
                .email("child@test.com")
                .name("아들")
                .userType("CHILD")
                .status("ACTIVE")
                .build());


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
        Account parentAccount = accountService.createAccount(
                parent.getId(),
                "CHECKING",
                "부모월급통장",
                BigDecimal.ZERO);
        // 자녀 계좌 생성 (주 계좌 설정됨)
        Account childAccount = accountService.createAccount(
                child.getId(),
                "SPENDING",
                "자녀용돈통장",
                BigDecimal.ZERO);

        // 부모 계좌에 10만원 입금 (외부 입금 가정)
        transactionService.createTransaction(
                parentAccount.getId(),
                "DEPOSIT",
                new BigDecimal("100000"),
                "월급",
                "입금",
                null);

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
    

    @Test
    @DisplayName("특정 자녀(Owner)의 미션 목록 조회 테스트")
    void getMissionsForOwner() {
        // 1. 사용자 준비
        User creator = userRepository.save(User.builder().firebaseUid("p2").email("p2@test.com").userType("PARENT").name("부모2").status("ACTIVE").build());
        User owner = userRepository.save(User.builder().firebaseUid("c2").email("c2@test.com").userType("CHILD").name("자녀2").status("ACTIVE").build());

        // 2. 미션 2개 생성
        createMockMission(creator, owner, "미션1");
        createMockMission(creator, owner, "미션2");

        // 3. 자녀 ID로 조회
        var missions = missionService.getMissionsForOwner(owner.getId());

        // 4. 검증
        assertThat(missions).hasSize(2);
        assertThat(missions).extracting("title")
                .containsExactlyInAnyOrder("미션1", "미션2");
    }

    @Test
    @DisplayName("부모(Creator)가 생성한 미션 목록 조회 테스트")
    void getMissionsCreatedBy() {
        // 1. 사용자 준비
        User creator = userRepository.save(User.builder().firebaseUid("p3").email("p3@test.com").userType("PARENT").name("부모3").status("ACTIVE").build());
        User owner = userRepository.save(User.builder().firebaseUid("c3").email("c3@test.com").userType("CHILD").name("자녀3").status("ACTIVE").build());

        // 2. 미션 생성
        createMockMission(creator, owner, "부모가 만든 미션");

        // 3. 부모 ID로 조회
        var missions = missionService.getMissionsCreatedBy(creator.getId());

        // 4. 검증
        assertThat(missions).hasSize(1);
        assertThat(missions.get(0).getCreator().getId()).isEqualTo(creator.getId());
    }

    @Test
    @DisplayName("미션 완료 실패 - 자녀의 주 계좌가 없는 경우 예외 발생")
    void completeMission_Fail_NoAccount() {
        // 1. 유저 생성 (계좌 생성 안 함)
        User creator = userRepository.save(User.builder().firebaseUid("p4").email("p4@test.com").userType("PARENT").name("부모4").status("ACTIVE").build());
        User owner = userRepository.save(User.builder().firebaseUid("c4").email("c4@test.com").userType("CHILD").name("자녀4").status("ACTIVE").build());

        // 2. 미션 생성
        MissionRequest request = MissionRequest.builder()
                .creatorId(creator.getId())
                .ownerId(owner.getId())
                .missionType("STUDY")
                .title("공부하기")
                .rewardAmount(new BigDecimal("500"))
                .status("IN_PROGRESS")
                .build();
        Mission mission = missionService.createMission(request);

        // 3. 완료 시도 및 검증 (계좌가 없으므로 실패해야 함)
        // RuntimeException 혹은 CustomException이 발생할 것으로 예상
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                missionService.completeMission(mission.getId())
        ).isInstanceOf(RuntimeException.class);
        // 실제 코드에서 Primary account not found 시 던지는 예외 타입에 맞춰 수정 필요
    }

    @Test
    @DisplayName("미션 완료 실패 - 이미 완료된 미션 중복 완료 시도")
    void completeMission_Fail_AlreadyCompleted() {
        // 1. 유저 및 계좌 생성
        User creator = userRepository.save(User.builder().firebaseUid("p5").email("p5@test.com").userType("PARENT").name("부모5").status("ACTIVE").build());
        User owner = userRepository.save(User.builder().firebaseUid("c5").email("c5@test.com").userType("CHILD").name("자녀5").status("ACTIVE").build());

        accountRepository.save(Account.builder()
                .user(owner).accountType("SPENDING").accountName("지갑")
                .balance(BigDecimal.ZERO).active(true).primary(true).build());

        // 2. 미션 생성 및 1회 완료
        MissionRequest request = MissionRequest.builder()
                .creatorId(creator.getId()).ownerId(owner.getId()).missionType("ETC")
                .title("중복테스트").rewardAmount(BigDecimal.TEN).status("IN_PROGRESS").build();
        Mission mission = missionService.createMission(request);
        missionService.completeMission(mission.getId());

        // 3. 중복 완료 시도 및 검증
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                missionService.completeMission(mission.getId())
        ).hasMessageContaining("이미 완료된 미션");
        // 혹은 CustomException 클래스 체크: .isInstanceOf(CustomException.class)
    }

    // 테스트 헬퍼 메서드
    private void createMockMission(User creator, User owner, String title) {
        MissionRequest request = MissionRequest.builder()
                .creatorId(creator.getId())
                .ownerId(owner.getId())
                .missionType("SAVING")
                .title(title)
                .description("테스트 설명")
                .targetAmount(new BigDecimal("10000"))
                .rewardAmount(new BigDecimal("500"))
                .status("IN_PROGRESS")
                .targetDate(LocalDate.now().plusDays(3))
                .build();
        missionService.createMission(request);
    }
}