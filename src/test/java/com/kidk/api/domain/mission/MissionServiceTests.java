package com.kidk.api.domain.mission;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.repository.AccountRepository;
import com.kidk.api.domain.mission.dto.MissionRequest;
import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.repository.MissionRepository;
import com.kidk.api.domain.mission.service.MissionService;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MissionServiceTests {

    @Autowired
    private MissionService missionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private AccountRepository accountRepository;


    @Test
    @DisplayName("DTO를 이용한 미션 생성 테스트")
    void createMissionWithDto() {
        // 1. 사용자 데이터 준비 (부모/자녀)
        User creator = userRepository.save(
                User.builder()
                        .firebaseUid("p1")
                        .email("parent@test.com")
                        .userType("PARENT")
                        .name("부모")
                        .status("ACTIVE")
                        .build()
        );

        User owner = userRepository.save(
                User.builder()
                        .firebaseUid("c1")
                        .email("child@test.com")
                        .userType("CHILD")
                        .name("아이")
                        .status("ACTIVE")
                        .build()
        );

        // 2. MissionRequest DTO 생성
        MissionRequest request = MissionRequest.builder()
                .creatorId(creator.getId())
                .ownerId(owner.getId())
                .missionType("SAVING")
                .title("장난감 사기")
                .description("용돈을 아껴서 장난감을 사요")
                .targetAmount(new BigDecimal("30000"))
                .rewardAmount(new BigDecimal("1000"))
                .status("IN_PROGRESS")
                .targetDate(LocalDate.now().plusDays(7))
                .build();

        // 3. 서비스 호출
        Mission createdMission = missionService.createMission(request);

        // 4. 검증
        assertThat(createdMission.getId()).isNotNull();
        assertThat(createdMission.getTitle()).isEqualTo("장난감 사기");
        assertThat(createdMission.getCreator().getId()).isEqualTo(creator.getId());
        assertThat(createdMission.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(createdMission.getTargetAmount()).isEqualByComparingTo(new BigDecimal("30000"));
    }

    @Test
    @DisplayName("미션 완료 처리 테스트")
    void completeMission() {
        // 1. 유저 생성
        User creator = userRepository.save(
                User.builder()
                        .firebaseUid("p1")
                        .email("p@test.com")
                        .userType("PARENT")
                        .name("부모")
                        .status("ACTIVE")
                        .build()
        );

        User owner = userRepository.save(
                User.builder()
                        .firebaseUid("c1")
                        .email("c@test.com")
                        .userType("CHILD")
                        .name("아이")
                        .status("ACTIVE")
                        .build()
        );

        // 2. [중요] 자녀의 주 계좌 생성 (보상 지급을 위해 필수)
        accountRepository.save(
                Account.builder()
                        .user(owner)
                        .accountType("SPENDING")
                        .accountName("용돈기입장")
                        .balance(new BigDecimal("0"))
                        .active(true)
                        .primary(true) // 주 계좌 설정
                        .build()
        );

        // 3. MissionRequest DTO 생성
        MissionRequest request = MissionRequest.builder()
                .creatorId(creator.getId())
                .ownerId(owner.getId())
                .missionType("SAVING")
                .title("용돈 아끼기")
                .description(null)
                .targetAmount(new BigDecimal("5000"))
                .rewardAmount(new BigDecimal("1000"))
                .status("PENDING")
                .build();

        // 4. 미션 생성
        Mission mission = missionService.createMission(request);

        // 5. 미션 완료 처리 (이제 계좌가 있으므로 성공함)
        Mission completed = missionService.completeMission(mission.getId());

        // 6. 검증
        assertThat(completed.getStatus()).isEqualTo("COMPLETED");
        assertThat(completed.getCompletedAt()).isNotNull();
    }
}
