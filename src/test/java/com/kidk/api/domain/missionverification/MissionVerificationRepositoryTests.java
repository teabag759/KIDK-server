package com.kidk.api.domain.missionverification;

import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.repository.MissionRepository;
import com.kidk.api.domain.missionverification.entity.MissionVerification;
import com.kidk.api.domain.missionverification.repository.MissionVerificationRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MissionVerificationRepositoryTests {

    @Autowired
    private MissionVerificationRepository verificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Test
    @DisplayName("미션 인증 생성 및 조회")
    void createAndFind() {
        User child = userRepository.save(
                User.builder()
                        .firebaseUid("child-123")
                        .email("child@example.com")
                        .name("아이")
                        .userType("CHILD")
                        .status("ACTIVE")
                        .build()
        );

        Mission mission = missionRepository.save(
                Mission.builder()
                        .creator(child)
                        .owner(child)
                        .missionType("PHOTO")
                        .title("테스트 미션")
                        .rewardAmount(new java.math.BigDecimal("500"))
                        .status("IN_PROGRESS")
                        .build()
        );

        MissionVerification saved = verificationRepository.save(
                MissionVerification.builder()
                        .mission(mission)
                        .child(child)
                        .verificationType("TEXT")
                        .content("오늘 청소했어요!")
                        .status("PENDING")
                        .build()
        );

        List<MissionVerification> list = verificationRepository.findByMissionId(mission.getId());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getContent()).isEqualTo("오늘 청소했어요!");
    }
}