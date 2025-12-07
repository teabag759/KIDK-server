package com.kidk.api.domain.savingsgoals;

import com.kidk.api.common.Status;
import com.kidk.api.domain.savingsgoal.dto.SavingsGoalRequest;
import com.kidk.api.domain.savingsgoal.entity.SavingsGoal;
import com.kidk.api.domain.savingsgoal.repository.SavingsGoalRepository;
import com.kidk.api.domain.savingsgoal.service.SavingsGoalService;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SavingsGoalServiceTests {
    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("저축 목표 생성 성공 - 초기값 확인")
    void createGoal_Success() {
        // 1. 준비
        User kid = createUser("kid_saver");
        SavingsGoalRequest request = SavingsGoalRequest.builder()
                .userId(kid.getId())
                .goalName("닌텐도 스위치") // DTO 필드명 확인 (title -> goalName)
                .targetAmount(new BigDecimal("360000"))
                .targetDate(LocalDateTime.now().plusMonths(6))
                .build();

        // 2. 실행
        SavingsGoal goal = savingsGoalService.createGoal(request);

        // 3. 검증
        assertThat(goal.getId()).isNotNull();
        assertThat(goal.getGoalName()).isEqualTo("닌텐도 스위치");
        assertThat(goal.getCurrentAmount()).isEqualByComparingTo("0"); // 초기 0원
        assertThat(goal.getStatus()).isEqualTo(Status.IN_PROGRESS); // 진행 중
    }

    @Test
    @DisplayName("저축 금액 추가 - 진행 중 상태 유지")
    void addAmount_InProgress() {
        // 1. 준비 (목표금액 10,000원)
        User kid = createUser("kid_add");
        SavingsGoal goal = createGoal(kid, "장난감", new BigDecimal("10000"));

        // 2. 실행 (3,000원 추가)
        SavingsGoal updatedGoal = savingsGoalService.addAmount(goal.getId(), new BigDecimal("3000"));

        // 3. 검증
        assertThat(updatedGoal.getCurrentAmount()).isEqualByComparingTo("3000");
        assertThat(updatedGoal.getStatus()).isEqualTo(Status.IN_PROGRESS); // 아직 달성 못함
    }

    @Test
    @DisplayName("저축 금액 추가 - 목표 달성 시 상태 변경 (ACHIEVED)")
    void addAmount_Achieved() {
        // 1. 준비 (목표금액 10,000원)
        User kid = createUser("kid_done");
        SavingsGoal goal = createGoal(kid, "로봇", new BigDecimal("10000"));

        // 기존에 9,000원 있었다고 가정 (테스트 셋업)
        savingsGoalService.addAmount(goal.getId(), new BigDecimal("9000"));

        // 2. 실행 (1,000원 추가 -> 총 10,000원 달성)
        SavingsGoal achievedGoal = savingsGoalService.addAmount(goal.getId(), new BigDecimal("1000"));

        // 3. 검증
        assertThat(achievedGoal.getCurrentAmount()).isEqualByComparingTo("10000");
        assertThat(achievedGoal.getStatus()).isEqualTo(Status.ACHIEVED); // 상태 변경 확인
        assertThat(achievedGoal.getAchievedAt()).isNotNull(); // 달성 시간 기록 확인
    }

    @Test
    @DisplayName("내 목표 목록 조회")
    void getMyGoals_Success() {
        // 1. 준비
        User kid = createUser("kid_list");
        createGoal(kid, "목표1", BigDecimal.TEN);
        createGoal(kid, "목표2", BigDecimal.TEN);

        // 2. 실행
        List<SavingsGoal> goals = savingsGoalService.getMyGoals(kid.getId());

        // 3. 검증
        assertThat(goals).hasSize(2);
    }

    @Test
    @DisplayName("목표 상세 조회 실패 - 존재하지 않는 ID")
    void getGoal_Fail_NotFound() {
        assertThatThrownBy(() ->
                savingsGoalService.getGoal(9999L)
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAVINGS_GOAL_NOT_FOUND);
    }


    // --- Helper Methods ---

    private User createUser(String uid) {
        return userRepository.save(User.builder()
                .firebaseUid(uid)
                .email(uid + "@test.com")
                .name(uid)
                .userType("CHILD")
                .status("ACTIVE")
                .build());
    }

    private SavingsGoal createGoal(User user, String name, BigDecimal target) {
        SavingsGoalRequest request = SavingsGoalRequest.builder()
                .userId(user.getId())
                .goalName(name)
                .targetAmount(target)
                .targetDate(LocalDateTime.now().plusMonths(1))
                .build();
        return savingsGoalService.createGoal(request);
    }
}
