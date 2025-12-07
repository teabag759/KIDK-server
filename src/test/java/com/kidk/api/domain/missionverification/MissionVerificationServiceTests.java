package com.kidk.api.domain.missionverification;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.repository.AccountRepository;
import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.repository.MissionRepository;
import com.kidk.api.domain.missionverification.entity.MissionVerification;
import com.kidk.api.domain.missionverification.service.MissionVerificationService;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MissionVerificationServiceTest {

    @Autowired
    private MissionVerificationService verificationService;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;


    @Test
    @DisplayName("인증 제출 성공 - 상태가 PENDING이어야 함")
    void submitVerification_Success() {
        // 1. 준비 (부모, 자녀, 미션)
        User parent = createUser("parent1", "PARENT");
        User child = createUser("child1", "CHILD");
        Mission mission = createMission(parent, child, "IN_PROGRESS");

        // 2. 실행 (자녀가 인증 제출)
        MissionVerification verification = verificationService.submitVerification(
                mission.getId(), child.getId(), "PHOTO", "image.jpg"
        );

        // 3. 검증
        assertThat(verification.getId()).isNotNull();
        assertThat(verification.getStatus()).isEqualTo("PENDING");
        assertThat(verification.getVerificationType()).isEqualTo("PHOTO");
    }

    @Test
    @DisplayName("부모 승인 성공 - 미션 완료 및 보상금 지급 확인")
    void approveVerification_Success() {
        // 1. 준비
        User parent = createUser("parent2", "PARENT");
        User child = createUser("child2", "CHILD");

        // [중요] 보상을 받으려면 자녀의 계좌가 있어야 함!
        Account childAccount = createAccount(child, new BigDecimal("0"));

        Mission mission = createMission(parent, child, "IN_PROGRESS");

        // 미리 인증 제출 해둠
        MissionVerification verification = verificationService.submitVerification(
                mission.getId(), child.getId(), "PHOTO", "done.jpg"
        );

        // 2. 실행 (부모가 승인)
        MissionVerification approved = verificationService.approveVerification(verification.getId(), parent.getId());

        // 3. 검증하기
        // 인증 상태 변경되었는지?
        assertThat(approved.getStatus()).isEqualTo("APPROVED");
        assertThat(approved.getReviewedBy().getId()).isEqualTo(parent.getId());

        // 미션 상태가 'COMPLETED'로 바꼈는지?
        Mission updatedMission = missionRepository.findById(mission.getId()).get();
        assertThat(updatedMission.getStatus()).isEqualTo("COMPLETED");

        // [핵심] 자녀 계좌에 보상금(1000원)이 들어왔는지?
        Account updatedAccount = accountRepository.findById(childAccount.getId()).get();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("1000"); // 0 + 1000
    }

    @Test
    @DisplayName("부모 거절 성공 - 미션은 여전히 진행 중이어야 함")
    void rejectVerification_Success() {
        // 1. 준비
        User parent = createUser("parent3", "PARENT");
        User child = createUser("child3", "CHILD");
        Mission mission = createMission(parent, child, "IN_PROGRESS");

        MissionVerification verification = verificationService.submitVerification(
                mission.getId(), child.getId(), "PHOTO", "bad_pic.jpg"
        );

        // 2. 실행 (부모 거절)
        MissionVerification rejected = verificationService.rejectVerification(
                verification.getId(), parent.getId(), "사진이 너무 어두워"
        );

        // 3. 검증
        assertThat(rejected.getStatus()).isEqualTo("REJECTED");
        assertThat(rejected.getRejectReason()).isEqualTo("사진이 너무 어두워");

        // 미션 상태는 여전히 IN_PROGRESS여야 함 (완료 X)
        Mission checkMission = missionRepository.findById(mission.getId()).get();
        assertThat(checkMission.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("인증 제출 실패 - 이미 완료된 미션")
    void submit_Fail_AlreadyCompleted() {
        User parent = createUser("p4", "PARENT");
        User child = createUser("c4", "CHILD");
        Mission mission = createMission(parent, child, "COMPLETED"); // 이미 완료된 상태

        assertThatThrownBy(() ->
                verificationService.submitVerification(mission.getId(), child.getId(), "PHOTO", "img.jpg")
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_COMPLETED_MISSION);
    }


    // --- Helper Methods ---

    private User createUser(String uid, String type) {
        return userRepository.save(User.builder()
                .firebaseUid(uid).email(uid + "@test.com").name(uid)
                .userType(type).status("ACTIVE").build());
    }

    private Mission createMission(User creator, User owner, String status) {
        return missionRepository.save(Mission.builder()
                .creator(creator).owner(owner)
                .title("청소").missionType("CLEANING")
                .rewardAmount(new BigDecimal("1000"))
                .targetAmount(BigDecimal.ZERO)
                .status(status)
                .targetDate(LocalDate.now().plusDays(1))
                .build());
    }

    private Account createAccount(User user, BigDecimal balance) {
        return accountRepository.save(Account.builder()
                .user(user).accountName("용돈통장").accountType("SPENDING")
                .balance(balance).active(true).primary(true)
                .build());
    }
}